package no.fintlabs.mapping

import no.fintlabs.gateway.instance.model.instance.Instance
import no.fintlabs.gateway.instance.model.instance.InstanceField
import no.fintlabs.model.EgrunnervervArchiveInstance
import spock.lang.Specification

class EgrunnervervArchiveInstanceMappingServiceSpec extends Specification {

    private EgrunnervervArchiveInstanceMappingService egrunnervervArchiveInstanceMappingService
    private EgrunnervervArchiveInstance egrunnervervArchiveInstance
    private Instance expectedInstance

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
                .saksansvarligEpost("testSaksansvarligEpost")
                .saksparter(List.of())
                .klasseringer(List.of())
                .build()

        expectedInstance = Instance
                .builder()
                .fieldPerKey(Map.of(
                        "sys_id", InstanceField.builder().key("sys_id").value("testSysId").build(),
                        "knr", InstanceField.builder().key("knr").value("testKnr").build(),
                        "gnr", InstanceField.builder().key("gnr").value("testGnr").build(),
                        "bnr", InstanceField.builder().key("bnr").value("testBnr").build(),
                        "fnr", InstanceField.builder().key("fnr").value("testFnr").build(),
                        "snr", InstanceField.builder().key("snr").value("testSnr").build(),
                        "takstnummer", InstanceField.builder().key("takstnummer").value("testTakstnummer").build(),
                        "tittel", InstanceField.builder().key("tittel").value("testTittel").build()
                ))
                .build()
    }

    def 'should map to instance'() {
        when:
        Instance instance = egrunnervervArchiveInstanceMappingService.map(1, egrunnervervArchiveInstance).block()

        then:
        instance == expectedInstance
    }

}
