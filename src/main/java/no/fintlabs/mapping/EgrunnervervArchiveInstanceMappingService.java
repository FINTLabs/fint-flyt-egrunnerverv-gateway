package no.fintlabs.mapping;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.Instance;
import no.fintlabs.gateway.instance.model.instance.InstanceField;
import no.fintlabs.model.EgrunnervervArchiveInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EgrunnervervArchiveInstanceMappingService implements InstanceMapper<EgrunnervervArchiveInstance> {
    @Override
    public Mono<Instance> map(Long aLong, EgrunnervervArchiveInstance egrunnervervArchiveInstance) {
        return Mono.just(Instance.builder()
                .fieldPerKey(Stream.of(
                                InstanceField.builder().key("sys_id").value(egrunnervervArchiveInstance.getSys_id()).build(),
                                InstanceField.builder().key("knr").value(egrunnervervArchiveInstance.getKnr()).build(),
                                InstanceField.builder().key("gnr").value(egrunnervervArchiveInstance.getGnr()).build(),
                                InstanceField.builder().key("bnr").value(egrunnervervArchiveInstance.getBnr()).build(),
                                InstanceField.builder().key("fnr").value(egrunnervervArchiveInstance.getFnr()).build(),
                                InstanceField.builder().key("snr").value(egrunnervervArchiveInstance.getSnr()).build(),
                                InstanceField.builder().key("takstnummer").value(egrunnervervArchiveInstance.getTakstnummer()).build(),
                                InstanceField.builder().key("tittel").value(egrunnervervArchiveInstance.getTittel()).build()
                        )
                        .collect(Collectors.toMap(
                                InstanceField::getKey,
                                Function.identity()
                        )))
                .build());
    }
}
