package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.ResourceRepository;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseRequestService;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.models.EgrunnervervJournalpostDocument;
import no.fintlabs.models.EgrunnervervJournalpostInstance;
import no.fintlabs.models.EgrunnervervJournalpostInstanceBody;
import no.fintlabs.models.EgrunnervervJournalpostReceiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EgrunnervervJournalpostInstanceMappingServiceTest {
    public static final long egrunnervervSourceApplicationId = 2;


    EgrunnervervJournalpostInstanceMappingService egrunnervervJournalpostInstanceMappingService;

    EgrunnervervJournalpostInstance egrunnervervJournalpostInstance;
    InstanceObject expectedInstance;
    @Mock
    ResourceRepository resourceRepository;

    @Mock
    ArchiveCaseRequestService archiveCaseRequestService;

    @Mock
    FileClient fileClient;

    @Mock
    SakResource sakResource;


    @BeforeEach
    public void setUp() {
        egrunnervervJournalpostInstance = EgrunnervervJournalpostInstance
                .builder()
                .saksnummer("testSaksnummer")
                .egrunnervervJournalpostInstanceBody(
                        EgrunnervervJournalpostInstanceBody
                                .builder()
                                .tittel("testTittel")
                                .dokumentNavn("testDokumentNavn")
                                .dokumentDato("testDokumentDato")
                                .forsendelsesMate("testForsendelsesmaate")
                                .kommunenavn("testKommunenavn")
                                .knr("testKnr")
                                .gnr("testGnr")
                                .bnr("testBnr")
                                .fnr("testFnr")
                                .snr("testSnr")
                                .eierforhold("testEierforhold")
                                .sysId("testSysId")
                                .id("testId")
                                .maltittel("testMaltittel")
                                .prosjektnavn("testProsjektnavn")
                                .saksbehandlerEpost("testSaksansvarligEpost")
                                .mottakere(List.of(
                                        EgrunnervervJournalpostReceiver
                                                .builder()
                                                .navn("testNavn")
                                                .organisasjonsnummer("testOrganisasjonsnummer")
                                                .epost("testEpost")
                                                .telefon("testTelefon")
                                                .postadresse("testPostadresse")
                                                .poststed("testPoststed")
                                                .postnummer("testPostnummer")
                                                .build())
                                )
                                .dokumenter(List.of(
                                        EgrunnervervJournalpostDocument
                                                .builder()
                                                .tittel("testHoveddokumentTittel")
                                                .hoveddokument(true)
                                                .filnavn("testHoveddokumentFilnavn.pdf")
                                                .dokumentBase64("SG92ZWRkb2t1bWVudA==")
                                                .build(),
                                        EgrunnervervJournalpostDocument
                                                .builder()
                                                .tittel("testVedlegg1Tittel")
                                                .hoveddokument(false)
                                                .filnavn("testVedlegg1Filnavn.pdf")
                                                .dokumentBase64("VmVkbGVnZzE=")
                                                .build(),
                                        EgrunnervervJournalpostDocument
                                                .builder()
                                                .tittel("testVedlegg2Tittel")
                                                .hoveddokument(false)
                                                .filnavn("testVedlegg2Filnavn.pdf")
                                                .dokumentBase64("VmVkbGVnZzI=")
                                                .build())
                                )
                                .build()
                )
                .build();

        HashMap<String, String> expectedInstanceValuePerKey = new HashMap<>();
        expectedInstanceValuePerKey.put("tittel", "testTittel");
        expectedInstanceValuePerKey.put("dokumentNavn", "testDokumentNavn");
        expectedInstanceValuePerKey.put("dokumentDato", "testDokumentDato");
        expectedInstanceValuePerKey.put("forsendelsesmaate", "testForsendelsesmaate");
        expectedInstanceValuePerKey.put("kommunenavn", "testKommunenavn");
        expectedInstanceValuePerKey.put("knr", "testKnr");
        expectedInstanceValuePerKey.put("gnr", "testGnr");
        expectedInstanceValuePerKey.put("bnr", "testBnr");
        expectedInstanceValuePerKey.put("fnr", "testFnr");
        expectedInstanceValuePerKey.put("snr", "testSnr");
        expectedInstanceValuePerKey.put("eierforhold", "testEierforhold");
        expectedInstanceValuePerKey.put("id", "testId");
        expectedInstanceValuePerKey.put("maltittel", "testMaltittel");
        expectedInstanceValuePerKey.put("prosjektnavn", "testProsjektnavn");
        expectedInstanceValuePerKey.put("saksbehandlerEpost", "testSaksansvarligEpost");
        expectedInstanceValuePerKey.put("saksnummer", "testSaksnummer");
        expectedInstanceValuePerKey.put("hoveddokumentFil", "251bfa61-6c0e-47d0-a479-643c40c3e766");
        expectedInstanceValuePerKey.put("hoveddokumentTittel", "testHoveddokumentTittel");
        expectedInstanceValuePerKey.put("hoveddokumentFilnavn", "testHoveddokumentFilnavn.pdf");
        expectedInstanceValuePerKey.put("hoveddokumentMediatype", "application/pdf");
        expectedInstanceValuePerKey.put("saksbehandler", "testSaksansvarlig");

        expectedInstance = InstanceObject
                .builder()
                .valuePerKey(expectedInstanceValuePerKey)
                .objectCollectionPerKey(
                        Map.of(
                                "mottakere", List.of(
                                        InstanceObject
                                                .builder()
                                                .valuePerKey(Map.of(
                                                        "navn", "testNavn",
                                                        "organisasjonsnummer", "testOrganisasjonsnummer",
                                                        "epost", "testEpost",
                                                        "telefon", "testTelefon",
                                                        "postadresse", "testPostadresse",
                                                        "poststed", "testPoststed",
                                                        "postnummer", "testPostnummer"
                                                ))
                                                .build()
                                ),
                                "vedlegg", List.of(
                                        InstanceObject
                                                .builder()
                                                .valuePerKey(Map.of(
                                                        "tittel", "testVedlegg1Tittel",
                                                        "filnavn", "testVedlegg1Filnavn.pdf",
                                                        "mediatype", "application/pdf",
                                                        "fil", "251bfa61-6c0e-47d0-a479-643c40c3e767"
                                                ))
                                                .build(),
                                        InstanceObject
                                                .builder()
                                                .valuePerKey(Map.of(
                                                        "tittel", "testVedlegg2Tittel",
                                                        "filnavn", "testVedlegg2Filnavn.pdf",
                                                        "mediatype", "application/pdf",
                                                        "fil", "251bfa61-6c0e-47d0-a479-643c40c3e768"
                                                ))
                                                .build()
                                )
                        )
                )
                .build();
    }

    @Test
    public void givenJournalpostWithHoveddokumentAndVedlegg_shouldReturnMappedInstanceAsExpected() {
        when(resourceRepository.getArkivressursHrefFromPersonEmail("testSaksansvarligEpost")).thenReturn(Optional.of("testSaksansvarlig"));
        when(archiveCaseRequestService.getByArchiveCaseId("testSaksnummer")).thenReturn(Optional.of(sakResource));

        ArgumentMatcher<File> argumentMatcherHoveddokument = file ->
                "testHoveddokumentFilnavn.pdf".equals(file.getName()) &&
                        "UTF-8".equals(file.getEncoding()) &&
                        "application/pdf".equals(String.valueOf(file.getType())) &&
                        "2".equals(String.valueOf(file.getSourceApplicationId())) &&
                        "testSysId".equals(file.getSourceApplicationInstanceId()) &&
                        "SG92ZWRkb2t1bWVudA==".equals(file.getBase64Contents());
        doReturn(Mono.just(UUID.fromString("251bfa61-6c0e-47d0-a479-643c40c3e766")))
                .when(fileClient).postFile(argThat(argumentMatcherHoveddokument));

        ArgumentMatcher<File> argumentMatcherVedlegg1 = file ->
                "testVedlegg1Filnavn.pdf".equals(file.getName()) &&
                        "UTF-8".equals(file.getEncoding()) &&
                        "application/pdf".equals(String.valueOf(file.getType())) &&
                        "2".equals(String.valueOf(file.getSourceApplicationId())) &&
                        "testSysId".equals(file.getSourceApplicationInstanceId()) &&
                        "VmVkbGVnZzE=".equals(file.getBase64Contents());
        doReturn(Mono.just(UUID.fromString("251bfa61-6c0e-47d0-a479-643c40c3e767")))
                .when(fileClient).postFile(argThat(argumentMatcherVedlegg1));

        ArgumentMatcher<File> argumentMatcherVedlegg2 = file ->
                "testVedlegg2Filnavn.pdf".equals(file.getName()) &&
                        "UTF-8".equals(file.getEncoding()) &&
                        "application/pdf".equals(String.valueOf(file.getType())) &&
                        "2".equals(String.valueOf(file.getSourceApplicationId())) &&
                        "testSysId".equals(file.getSourceApplicationInstanceId()) &&
                        "VmVkbGVnZzI=".equals(file.getBase64Contents());
        doReturn(Mono.just(UUID.fromString("251bfa61-6c0e-47d0-a479-643c40c3e768")))
                .when(fileClient).postFile(argThat(argumentMatcherVedlegg2));

        egrunnervervJournalpostInstanceMappingService = new EgrunnervervJournalpostInstanceMappingService(archiveCaseRequestService, fileClient, resourceRepository);
        egrunnervervJournalpostInstanceMappingService.checkSaksbehandler = true;

        InstanceObject instanceObject = egrunnervervJournalpostInstanceMappingService.map(egrunnervervSourceApplicationId, egrunnervervJournalpostInstance).block();
        assertThat(instanceObject).isEqualTo(expectedInstance);

        verify(fileClient, times(1)).postFile(argThat(argumentMatcherHoveddokument));
        verify(fileClient, times(1)).postFile(argThat(argumentMatcherVedlegg1));
        verify(fileClient, times(1)).postFile(argThat(argumentMatcherVedlegg2));
        verifyNoMoreInteractions(fileClient);


    }

}