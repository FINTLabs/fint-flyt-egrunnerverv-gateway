package no.fintlabs.mapping

import no.fintlabs.gateway.instance.model.instance.Instance
import no.fintlabs.gateway.instance.model.instance.InstanceField
import no.fintlabs.model.EgrunnervervDocumentInstance
import spock.lang.Specification

class EgrunnervervDocumentInstanceMappingServiceSpec extends Specification {

    private EgrunnervervDocumentInstanceMappingService egrunnervervDocumentInstanceMappingService
    private EgrunnervervDocumentInstance egrunnervervDocumentInstance
    private Instance expectedInstance

    def setup() {
        egrunnervervDocumentInstanceMappingService = new EgrunnervervDocumentInstanceMappingService()

        egrunnervervDocumentInstance = EgrunnervervDocumentInstance
                .builder()
                .tittel("testTittel")
                .dokumentDato("testDokumentDato")
                .dokumentTypeId("testDokumentTypeId")
                .dokumentkategoriId("testDokumentkategoriId")
                .tilgangskode("testTilgangskode")
                .hjemmel("testHjemmel")
                .merknad("testMerknad")
                .avskrivDirekte("testAvskrivDirekte")
                .forsendelsesmaate("testForsendelsesmaate")
                .avsender("testAvsender")
                .build()

        expectedInstance = Instance
                .builder()
                .fieldPerKey(Map.of(
                        "tittel", InstanceField.builder().key("tittel").value("testTittel").build(),
                        "dokumentDato", InstanceField.builder().key("dokumentDato").value("testDokumentDato").build(),
                        "dokumentTypeId", InstanceField.builder().key("dokumentTypeId").value("testDokumentTypeId").build(),
                        "dokumentkategoriId", InstanceField.builder().key("dokumentkategoriId").value("testDokumentkategoriId").build(),
                        "tilgangskode", InstanceField.builder().key("tilgangskode").value("testTilgangskode").build(),
                        "hjemmel", InstanceField.builder().key("hjemmel").value("testHjemmel").build(),
                        "merknad", InstanceField.builder().key("merknad").value("testMerknad").build(),
                        "avskrivDirekte", InstanceField.builder().key("avskrivDirekte").value("testAvskrivDirekte").build(),
                        "forsendelsesmaate", InstanceField.builder().key("forsendelsesmaate").value("testForsendelsesmaate").build(),
                        "avsender", InstanceField.builder().key("avsender").value("testAvsender").build()
                ))
                .build()
    }

    def 'should map to instance'() {
        when:
        Instance instance = egrunnervervDocumentInstanceMappingService.map(1, egrunnervervDocumentInstance).block()

        then:
        instance == expectedInstance
    }

}
