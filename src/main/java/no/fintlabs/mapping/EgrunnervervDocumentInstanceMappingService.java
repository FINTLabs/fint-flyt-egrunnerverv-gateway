package no.fintlabs.mapping;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.Instance;
import no.fintlabs.gateway.instance.model.instance.InstanceField;
import no.fintlabs.model.EgrunnervervDocumentInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EgrunnervervDocumentInstanceMappingService implements InstanceMapper<EgrunnervervDocumentInstance> {
    @Override
    public Mono<Instance> map(Long aLong, EgrunnervervDocumentInstance egrunnervervDocumentInstance) {
        return Mono.just(Instance.builder()
                .fieldPerKey(Stream.of(
                                InstanceField.builder().key("tittel").value(egrunnervervDocumentInstance.getTittel()).build(),
                                InstanceField.builder().key("dokumentDato").value(egrunnervervDocumentInstance.getDokumentDato()).build(),
                                InstanceField.builder().key("dokumentTypeId").value(egrunnervervDocumentInstance.getDokumentTypeId()).build(),
                                InstanceField.builder().key("dokumentkategoriId").value(egrunnervervDocumentInstance.getDokumentkategoriId()).build(),
                                InstanceField.builder().key("tilgangskode").value(egrunnervervDocumentInstance.getTilgangskode()).build(),
                                InstanceField.builder().key("hjemmel").value(egrunnervervDocumentInstance.getHjemmel()).build(),
                                InstanceField.builder().key("merknad").value(egrunnervervDocumentInstance.getMerknad()).build(),
                                InstanceField.builder().key("avskrivDirekte").value(egrunnervervDocumentInstance.getAvskrivDirekte()).build(),
                                InstanceField.builder().key("forsendelsesmaate").value(egrunnervervDocumentInstance.getForsendelsesmaate()).build(),
                                InstanceField.builder().key("avsender").value(egrunnervervDocumentInstance.getAvsender()).build()
                        )
                        .collect(Collectors.toMap(
                                InstanceField::getKey,
                                Function.identity()
                        )))
                .build());
    }
}
