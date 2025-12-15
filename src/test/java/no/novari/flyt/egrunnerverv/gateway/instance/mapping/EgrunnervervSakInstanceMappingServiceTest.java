package no.novari.flyt.egrunnerverv.gateway.instance.mapping;

import no.novari.flyt.egrunnerverv.gateway.exception.ArchiveResourceNotFoundException;
import no.novari.flyt.egrunnerverv.gateway.exception.NonMatchingEmailDomainWithOrgIdException;
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakKlassering;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSaksPart;
import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService;
import no.novari.flyt.instance.gateway.model.File;
import no.novari.flyt.instance.gateway.model.InstanceObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EgrunnervervSakInstanceMappingServiceTest {

    private ResourceRepository resourceRepository;
    private FormattingUtilsService formattingUtilsService;

    private EgrunnervervSakInstanceMappingService service;

    private Function<File, Mono<UUID>> persistFile;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        resourceRepository = mock(ResourceRepository.class);
        formattingUtilsService = mock(FormattingUtilsService.class);
        SlackAlertService slackAlertService = mock(SlackAlertService.class);

        persistFile = mock(Function.class);

        service = new EgrunnervervSakInstanceMappingService(
                resourceRepository,
                formattingUtilsService,
                slackAlertService
        );

        setField(service, "orgId", "novari.no");
        service.checkEmailDomain = true;
        service.checkSaksansvarligEpost = true;

        lenient().when(persistFile.apply(any()))
                .thenReturn(Mono.just(UUID.randomUUID()));

        lenient().when(formattingUtilsService.formatKommunenavn(any()))
                .thenReturn("TestKommunenavn");

        lenient().when(formattingUtilsService.formatEmail(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        lenient().when(formattingUtilsService.extractEmailDomain(anyString()))
                .thenReturn("novari.no");
    }

    private EgrunnervervSakInstance createInstance() {
        return EgrunnervervSakInstance.builder()
                .sysId("sys1")
                .knr("1")
                .gnr("2")
                .bnr("3")
                .fnr("4")
                .snr("5")
                .takstnummer("6")
                .tittel("Tittel")
                .saksansvarligEpost("person@novari.no")
                .eierforholdsnavn("Eiernavn")
                .eierforholdskode("KODE")
                .prosjektnr("42")
                .prosjektnavn("Prosjekt")
                .kommunenavn("OSLO")
                .adresse("Gate 1")
                .saksparter(List.of(
                        EgrunnervervSaksPart.builder()
                                .navn("P1")
                                .organisasjonsnummer("999999999")
                                .epost("p1@novari.no")
                                .telefon("11111111")
                                .postadresse("Adr1")
                                .postnummer("1000")
                                .poststed("Oslo")
                                .build()
                ))
                .klasseringer(List.of(
                        EgrunnervervSakKlassering.builder()
                                .ordningsprinsipp("OP")
                                .ordningsverdi("OV")
                                .beskrivelse("Beskrivelse")
                                .sortering("1")
                                .untattOffentlighet("false")
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("Maps instance correctly when lookup succeeds")
    void shouldMapInstance() {
        when(resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no"))
                .thenReturn(Optional.of("saksansvarligHref"));

        EgrunnervervSakInstance instance = createInstance();

        InstanceObject result = service.map(123L, instance, persistFile).block();

        assertThat(result).isNotNull();
        assertThat(result.getValuePerKey())
                .containsEntry("sys_id", "sys1")
                .containsEntry("saksansvarlig", "saksansvarligHref")
                .containsEntry("kommunenavn", "TestKommunenavn");

        assertThat(result.getObjectCollectionPerKey())
                .containsKeys("saksparter", "klasseringer");

        assertThat(result.getObjectCollectionPerKey().get("saksparter"))
                .hasSize(1);

        assertThat(result.getObjectCollectionPerKey().get("klasseringer"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Throws when email domain does not match orgId")
    void shouldThrowWhenEmailDomainDoesNotMatch() {
        setField(service, "orgId", "novari.no");
        EgrunnervervSakInstance instance = createInstance()
                .toBuilder()
                .saksansvarligEpost("person@wrong.no")
                .build();

        when(formattingUtilsService.extractEmailDomain("person@wrong.no"))
                .thenReturn("wrong.no");

        assertThatThrownBy(() ->
                service.map(123L, instance, persistFile).block()
        ).isInstanceOf(NonMatchingEmailDomainWithOrgIdException.class);
    }

    @Test
    @DisplayName("Throws when archive resource is missing")
    void shouldThrowWhenArchiveResourceMissing() {
        EgrunnervervSakInstance instance = createInstance();

        when(resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.map(123L, instance, persistFile).block()
        ).isInstanceOf(ArchiveResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Skips email domain validation when checkEmailDomain=false")
    void shouldSkipEmailDomainCheck() {
        service.checkEmailDomain = false;

        EgrunnervervSakInstance instance = createInstance()
                .toBuilder()
                .saksansvarligEpost("person@wrong.no")
                .build();

        when(resourceRepository.getArkivressursHrefFromPersonEmail(any()))
                .thenReturn(Optional.of("href"));

        InstanceObject result = service.map(123L, instance, persistFile).block();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Skips archive lookup when checkSaksansvarligEpost=false")
    void shouldSkipArchiveLookup() {
        service.checkSaksansvarligEpost = false;

        EgrunnervervSakInstance instance = createInstance();

        InstanceObject result = service.map(123L, instance, persistFile).block();

        Assertions.assertNotNull(result);
        assertThat(result.getValuePerKey())
                .containsEntry("saksansvarlig", "");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}