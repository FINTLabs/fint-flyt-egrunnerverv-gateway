package no.novari.flyt.egrunnerverv.gateway.instance.mapping;

import no.novari.flyt.egrunnerverv.gateway.exception.ArchiveResourceNotFoundException;
import no.novari.flyt.egrunnerverv.gateway.exception.NonMatchingEmailDomainWithOrgIdException;
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostDocument;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstanceBody;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostReceiver;
import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService;
import no.novari.flyt.instance.gateway.model.InstanceObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EgrunnervervJournalpostInstanceMappingServiceTest {

    private ResourceRepository resourceRepository;
    private FormattingUtilsService formattingUtilsService;

    private EgrunnervervJournalpostInstanceMappingService service;
    private Function<no.novari.flyt.instance.gateway.model.File, Mono<UUID>> persistFile;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        resourceRepository = mock(ResourceRepository.class);
        formattingUtilsService = mock(FormattingUtilsService.class);
        SlackAlertService slackAlertService = mock(SlackAlertService.class);

        persistFile = (Function<no.novari.flyt.instance.gateway.model.File, Mono<UUID>>) mock(Function.class);

        service = new EgrunnervervJournalpostInstanceMappingService(
                resourceRepository,
                formattingUtilsService,
                slackAlertService
        );

        setPrivateField(service, "orgId", "novari.no");
        setPrivateField(service, "checkEmailDomain", true);
        setPrivateField(service, "checkSaksbehandler", true);

        lenient().when(formattingUtilsService.formatEmail(anyString()))
                .thenAnswer(inv -> inv.getArgument(0));

        lenient().when(formattingUtilsService.extractEmailDomain(anyString()))
                .thenReturn("novari.no");

        lenient().when(formattingUtilsService.formatKommunenavn(anyString()))
                .thenReturn("FormattedKommune");

        lenient().when(persistFile.apply(any()))
                .thenReturn(Mono.just(UUID.randomUUID()));
    }

    private void setPrivateField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private EgrunnervervJournalpostInstance createInstance() {
        return createInstance("person@novari.no");
    }

    private EgrunnervervJournalpostInstance createInstance(String saksbehandlerEpost) {
        EgrunnervervJournalpostDocument hoveddok = EgrunnervervJournalpostDocument.builder()
                .tittel("Hoveddok")
                .filnavn("hoved.pdf")
                .dokumentBase64("AAAA")
                .hoveddokument(true)
                .build();

        EgrunnervervJournalpostDocument vedlegg = EgrunnervervJournalpostDocument.builder()
                .tittel("Vedlegg1")
                .filnavn("vedlegg1.pdf")
                .dokumentBase64("BBBB")
                .hoveddokument(false)
                .build();

        EgrunnervervJournalpostReceiver receiver = EgrunnervervJournalpostReceiver.builder()
                .navn("TestNavn")
                .organisasjonsnummer("123456789")
                .epost("test@novari.no")
                .build();

        EgrunnervervJournalpostInstanceBody body = EgrunnervervJournalpostInstanceBody.builder()
                .sysId("SYS123")
                .tittel("Sakstittel")
                .dokumentNavn("Doknavn")
                .saksbehandlerEpost(saksbehandlerEpost)
                .kommunenavn("OSLO")
                .dokumenter(List.of(hoveddok, vedlegg))
                .mottakere(List.of(receiver))
                .build();

        return EgrunnervervJournalpostInstance.builder()
                .saksnummer("SAK123")
                .egrunnervervJournalpostInstanceBody(body)
                .build();
    }

    @Test
    @DisplayName("Maps full instance successfully")
    void testHappyPathMapping() {
        when(resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no"))
                .thenReturn(Optional.of("href123"));

        EgrunnervervJournalpostInstance instance = createInstance();

        InstanceObject result = service.map(10L, instance, persistFile).block();

        assertThat(result).isNotNull();
        assertThat(result.getValuePerKey())
                .containsEntry("saksnummer", "SAK123")
                .containsEntry("kommunenavn", "FormattedKommune")
                .containsEntry("saksbehandler", "href123");

        assertThat(result.getObjectCollectionPerKey().get("vedlegg")).hasSize(1);
        assertThat(result.getObjectCollectionPerKey().get("mottakere")).hasSize(1);

        verify(persistFile, times(2)).apply(any());
    }

    @Test
    @DisplayName("Throws when email domain is wrong")
    void testWrongEmailDomain() {
        when(formattingUtilsService.extractEmailDomain("person@wrong.no"))
                .thenReturn("wrong.no");

        EgrunnervervJournalpostInstance instance = createInstance("person@wrong.no");

        assertThatThrownBy(() ->
                service.map(10L, instance, persistFile).block()
        ).isInstanceOf(NonMatchingEmailDomainWithOrgIdException.class);

        verify(resourceRepository, never()).getArkivressursHrefFromPersonEmail(any());
    }

    @Test
    @DisplayName("Throws when archive resource is missing")
    void testArchiveResourceMissing() {
        when(resourceRepository.getArkivressursHrefFromPersonEmail(anyString()))
                .thenReturn(Optional.empty());

        EgrunnervervJournalpostInstance instance = createInstance();

        assertThatThrownBy(() ->
                service.map(10L, instance, persistFile).block()
        ).isInstanceOf(ArchiveResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Skips domain check when checkEmailDomain=false")
    void testSkipDomainCheck() throws Exception {
        setPrivateField(service, "checkEmailDomain", false);

        when(resourceRepository.getArkivressursHrefFromPersonEmail(anyString()))
                .thenReturn(Optional.of("href123"));

        EgrunnervervJournalpostInstance instance = createInstance("person@other.no");

        InstanceObject result = service.map(1L, instance, persistFile).block();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Skips saksbehandler lookup when checkSaksbehandler=false")
    void testSkipSaksbehandlerLookup() throws Exception {
        setPrivateField(service, "checkSaksbehandler", false);

        EgrunnervervJournalpostInstance instance = createInstance();

        InstanceObject result = service.map(1L, instance, persistFile).block();

        Assertions.assertNotNull(result);
        assertThat(result.getValuePerKey())
                .containsEntry("saksbehandler", "");
    }
}
