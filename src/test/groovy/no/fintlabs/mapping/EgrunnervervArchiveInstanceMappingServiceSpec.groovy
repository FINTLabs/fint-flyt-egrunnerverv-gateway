package no.fintlabs.mapping


import no.fintlabs.gateway.instance.model.instance.InstanceElement
import no.fintlabs.model.EgrunnervervArchiveCasePart
import no.fintlabs.model.EgrunnervervArchiveClassification
import no.fintlabs.model.EgrunnervervArchiveInstance
import spock.lang.Specification

class EgrunnervervArchiveInstanceMappingServiceSpec extends Specification {

    private EgrunnervervArchiveInstanceMappingService egrunnervervArchiveInstanceMappingService
    private EgrunnervervArchiveInstance egrunnervervArchiveInstance
    private InstanceElement expectedInstance

    def setup() {
        egrunnervervArchiveInstanceMappingService = new EgrunnervervArchiveInstanceMappingService()

        egrunnervervArchiveInstance = EgrunnervervArchiveInstance
                .builder()
                .sys_id("testSysId")
                .knr("testKnr")
                .gnr("testGnr")
                .bnr("testBnr")
                .fnr("testFnr")
                .snr("testSnr")
                .takstnummer("testTakstnummer")
                .tittel("testTittel")
                .eierforholdsnavn("testEierforholdsnavn")
                .eierforholdskode("")
                .prosjektnr(null)
                .prosjektnavn("testProsjektnavn")
                .kommunenavn("testKommunenavn")
                .adresse("testAdresse")
                .saksparter(List.of(
                        EgrunnervervArchiveCasePart
                                .builder()
                                .sakspartRolleId("test1SakspartRolleId")
                                .navn("test1Navn")
                                .organisasjonsnummer("test1Organisasjonsnr")
                                .epost("test1Epost")
                                .telefon("test1Telefon")
                                .postadresse("test1Postadresse")
                                .postnummer("test1Postnummer")
                                .poststed("test1Poststed")
                                .build(),
                        EgrunnervervArchiveCasePart
                                .builder()
                                .sakspartRolleId("test2SakspartRolleId")
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
                        EgrunnervervArchiveClassification
                                .builder()
                                .ordningsprinsipp("test1Ordningsprinsipp")
                                .ordningsverdi("test1Ordningsverdi")
                                .beskrivelse("test1Beskrivelse")
                                .sortering("test1Sortering")
                                .untattOffentlighet("test1UntattOffentlighet")
                                .build(),
                        EgrunnervervArchiveClassification
                                .builder()
                                .ordningsprinsipp("test2Ordningsprinsipp")
                                .ordningsverdi("test2Ordningsverdi")
                                .beskrivelse("test2Beskrivelse")
                                .sortering("test2Sortering")
                                .untattOffentlighet("test2UntattOffentlighet")
                                .build()

                ))
                .build()

        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("sys_id", "testSysId");
        valuePerKey.put("knr", "testKnr");
        valuePerKey.put("gnr", "testGnr");
        valuePerKey.put("bnr", "testBnr");
        valuePerKey.put("fnr", "testFnr");
        valuePerKey.put("snr", "testSnr");
        valuePerKey.put("takstnummer", "testTakstnummer");
        valuePerKey.put("tittel", "testTittel");
        valuePerKey.put("eierforholdsnavn", "testEierforholdsnavn");
        valuePerKey.put("eierforholdskode", "");
        valuePerKey.put("prosjektnr", null);
        valuePerKey.put("prosjektnavn", "testProsjektnavn");
        valuePerKey.put("kommunenavn", "testKommunenavn");
        valuePerKey.put("adresse", "testAdresse");
        expectedInstance = InstanceElement
                .builder()
                .valuePerKey(valuePerKey)
                .elementCollectionPerKey(

                        Map.of(
                                "saksparter", List.of(
                                InstanceElement
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "sakspartRolleId", "test1SakspartRolleId",
                                                "navn", "test1Navn",
                                                "organisasjonsnummer", "test1Organisasjonsnr",
                                                "epost", "test1Epost",
                                                "telefon", "test1Telefon",
                                                "postadresse", "test1Postadresse",
                                                "postnummer", "test1Postnummer",
                                                "poststed", "test1Poststed"
                                        ))
                                        .build(),
                                InstanceElement
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "sakspartRolleId", "test2SakspartRolleId",
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
                                InstanceElement
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "ordningsprinsipp", "test1Ordningsprinsipp",
                                                "ordningsverdi", "test1Ordningsverdi",
                                                "beskrivelse", "test1Beskrivelse",
                                                "sortering", "test1Sortering",
                                                "untattOffentlighet", "test1UntattOffentlighet"
                                        ))
                                        .build(),
                                InstanceElement
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
        InstanceElement instanceElement = egrunnervervArchiveInstanceMappingService.map(1, egrunnervervArchiveInstance).block()

        then:
        instanceElement == expectedInstance
    }

}
