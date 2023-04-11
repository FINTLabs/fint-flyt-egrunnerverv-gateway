package no.fintlabs.mapping

import no.fintlabs.ResourceRepository
import no.fintlabs.gateway.instance.model.instance.InstanceObject
import no.fintlabs.models.EgrunnervervSakInstance
import no.fintlabs.models.EgrunnervervSakKlassering
import no.fintlabs.models.EgrunnervervSaksPart
import spock.lang.Specification

class EgrunnervervSakInstanceMappingServiceSpec extends Specification {

    public static final int egrunnervervSourceApplicationId = 2
    private EgrunnervervSakInstanceMappingService egrunnervervSakInstanceMappingService
    private EgrunnervervSakInstance egrunnervervSakInstance
    private InstanceObject expectedInstance
    private ResourceRepository resourceRepository

    def setup() {
        resourceRepository = Mock(ResourceRepository.class)
        resourceRepository.getSaksansvarligHref("testSaksansvarligEpost") >> Optional.of("testSaksansvarlig")

        egrunnervervSakInstanceMappingService = new EgrunnervervSakInstanceMappingService(resourceRepository)

        egrunnervervSakInstance = EgrunnervervSakInstance
                .builder()
                .sysId("testSysId")
                .knr("testKnr")
                .gnr("testGnr")
                .bnr("testBnr")
                .fnr("testFnr")
                .snr("testSnr")
                .takstnummer("testTakstnummer")
                .tittel("testTittel")
                .saksansvarligEpost("testSaksansvarligEpost")
                .eierforholdsnavn("testEierforholdsnavn")
                .eierforholdskode("")
                .prosjektnr(null)
                .prosjektnavn("testProsjektnavn")
                .kommunenavn("testKommunenavn")
                .adresse("testAdresse")
                .saksparter(List.of(
                        EgrunnervervSaksPart
                                .builder()
                                .navn("test1Navn")
                                .organisasjonsnummer("test1Organisasjonsnr")
                                .epost("test1Epost")
                                .telefon("test1Telefon")
                                .postadresse("test1Postadresse")
                                .postnummer("test1Postnummer")
                                .poststed("test1Poststed")
                                .build(),
                        EgrunnervervSaksPart
                                .builder()
                                .navn("test2Navn")
                                .organisasjonsnummer("test2Organisasjonsnr")
                                .epost("test2Epost")
                                .telefon("test2Telefon")
                                .postadresse("test2Postadresse")
                                .postnummer("test2Postnummer")
                                .poststed("test2Poststed")
                                .build()
                ))
                .klasseringer(List.of(
                        EgrunnervervSakKlassering
                                .builder()
                                .ordningsprinsipp("test1Ordningsprinsipp")
                                .ordningsverdi("test1Ordningsverdi")
                                .beskrivelse("test1Beskrivelse")
                                .sortering("test1Sortering")
                                .untattOffentlighet("test1UntattOffentlighet")
                                .build(),
                        EgrunnervervSakKlassering
                                .builder()
                                .ordningsprinsipp("test2Ordningsprinsipp")
                                .ordningsverdi("test2Ordningsverdi")
                                .beskrivelse("test2Beskrivelse")
                                .sortering("test2Sortering")
                                .untattOffentlighet("test2UntattOffentlighet")
                                .build()

                ))
                .build()

        Map<String, String> valuePerKey = new HashMap<>()
        valuePerKey.put("sys_id", "testSysId")
        valuePerKey.put("knr", "testKnr")
        valuePerKey.put("gnr", "testGnr")
        valuePerKey.put("bnr", "testBnr")
        valuePerKey.put("fnr", "testFnr")
        valuePerKey.put("snr", "testSnr")
        valuePerKey.put("takstnummer", "testTakstnummer")
        valuePerKey.put("tittel", "testTittel")
        valuePerKey.put("saksansvarligEpost", "testSaksansvarligEpost")
        valuePerKey.put("eierforholdsnavn", "testEierforholdsnavn")
        valuePerKey.put("eierforholdskode", "")
        valuePerKey.put("prosjektnr", null)
        valuePerKey.put("prosjektnavn", "testProsjektnavn")
        valuePerKey.put("kommunenavn", "testKommunenavn")
        valuePerKey.put("adresse", "testAdresse")
        valuePerKey.put("saksansvarlig", "testSaksansvarlig")
        expectedInstance = InstanceObject
                .builder()
                .valuePerKey(valuePerKey)
                .objectCollectionPerKey(

                        Map.of(
                                "saksparter", List.of(
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "test1Navn",
                                                "organisasjonsnummer", "test1Organisasjonsnr",
                                                "epost", "test1Epost",
                                                "telefon", "test1Telefon",
                                                "postadresse", "test1Postadresse",
                                                "postnummer", "test1Postnummer",
                                                "poststed", "test1Poststed"
                                        ))
                                        .build(),
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "test2Navn",
                                                "organisasjonsnummer", "test2Organisasjonsnr",
                                                "epost", "test2Epost",
                                                "telefon", "test2Telefon",
                                                "postadresse", "test2Postadresse",
                                                "postnummer", "test2Postnummer",
                                                "poststed", "test2Poststed"
                                        ))
                                        .build()

                        ),
                                "klasseringer", List.of(
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "ordningsprinsipp", "test1Ordningsprinsipp",
                                                "ordningsverdi", "test1Ordningsverdi",
                                                "beskrivelse", "test1Beskrivelse",
                                                "sortering", "test1Sortering",
                                                "untattOffentlighet", "test1UntattOffentlighet"
                                        ))
                                        .build(),
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "ordningsprinsipp", "test2Ordningsprinsipp",
                                                "ordningsverdi", "test2Ordningsverdi",
                                                "beskrivelse", "test2Beskrivelse",
                                                "sortering", "test2Sortering",
                                                "untattOffentlighet", "test2UntattOffentlighet"
                                        ))
                                        .build()

                        )
                        )
                ).build()
    }

    def 'should map to instance'() {
        when:
        InstanceObject instanceObject = egrunnervervSakInstanceMappingService.map(egrunnervervSourceApplicationId, egrunnervervSakInstance).block()

        then:
        instanceObject == expectedInstance
    }

}
