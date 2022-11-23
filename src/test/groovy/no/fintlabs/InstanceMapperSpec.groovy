package no.fintlabs


import no.fintlabs.model.egrunnerverv.EgrunnervervDocument
import no.fintlabs.model.egrunnerverv.EgrunnervervInstance
import no.fintlabs.model.egrunnerverv.EgrunnervervInstanceElement
import no.fintlabs.model.egrunnerverv.EgrunnervervInstanceMetadata
import no.fintlabs.model.fint.instance.Instance
import no.fintlabs.model.fint.instance.InstanceField
import spock.lang.Specification

class InstanceMapperSpec extends Specification {

    private FileProcessingService fileProcessingService
    private EgrunnervervInstanceMapper egrunnervervInstanceMapper
    private EgrunnervervInstance egrunnervervInstance
    private Instance expectedInstance

    def setup() {
        fileProcessingService = Mock(FileProcessingService.class)
        egrunnervervInstanceMapper = new EgrunnervervInstanceMapper(fileProcessingService)

        egrunnervervInstance = EgrunnervervInstance
                .builder()
                .metadata(
                        EgrunnervervInstanceMetadata
                                .builder()
                                .formId("arkivsak")
                                .instanceId("1234")
                                .instanceUri("https://egrunnerverv.com/form-instance?id=100384")
                                .build()
                )
                .elements(List.of(
                        EgrunnervervInstanceElement.builder().id("sys_id").value("507232e21bc2c910b074635ee54bcb6b").build(),
                        EgrunnervervInstanceElement.builder().id("knr").value("2222").build(),
                        EgrunnervervInstanceElement.builder().id("gnr").value("333").build(),
                        EgrunnervervInstanceElement.builder().id("bnr").value("4").build(),
                        EgrunnervervInstanceElement.builder().id("fnr").value("0").build(),
                        EgrunnervervInstanceElement.builder().id("takstsnummer").value("0").build(),
                        EgrunnervervInstanceElement.builder().id("tittel").value("TEST - E39 Mandal - Lyngdal øst - Grunnerverv - 2222 / 333 / 4, 0, 0, H - Testvei 321 3531 KROKKLEIVA - Heggernes Øystein").build(),
                        EgrunnervervInstanceElement.builder().id("saksansvarligEpost").value("ole-terje.pedersen@nyeveier.no").build(),
                ))
                .documents(Collections.emptyList())
                .build()

        expectedInstance = Instance
                .builder()
                .sourceApplicationInstanceUri("https://egrunnerverv.com/form-instance?id=100384")
                .fieldPerKey(Map.of(
                        "sys_id", InstanceField.builder().key("sys_id").value("507232e21bc2c910b074635ee54bcb6b").build(),
                        "knr", InstanceField.builder().key("knr").value("2222").build(),
                        "gnr", InstanceField.builder().key("gnr").value("333").build(),
                        "bnr", InstanceField.builder().key("bnr").value("4").build(),
                        "fnr", InstanceField.builder().key("fnr").value("0").build(),
                        "takstsnummer", InstanceField.builder().key("takstsnummer").value("0").build(),
                        "tittel", InstanceField.builder().key("tittel").value("TEST - E39 Mandal - Lyngdal øst - Grunnerverv - 2222 / 333 / 4, 0, 0, H - Testvei 321 3531 KROKKLEIVA - Heggernes Øystein").build(),
                        "saksansvarligEpost", InstanceField.builder().key("saksansvarligEpost").value("ole-terje.pedersen@nyeveier.no").build(),
                ))
                .documents(Collections.emptyList())
                .build()
    }

    def 'should map to instance'() {
        given:
        fileProcessingService.processFile(_ as EgrunnervervDocument) >> UUID.fromString("dab3ecc8-2901-46f0-9553-2fbc3e71ae9e")
        when:
        Instance instance = egrunnervervInstanceMapper.toInstance(egrunnervervInstance)

        then:
        instance == expectedInstance
    }

}
