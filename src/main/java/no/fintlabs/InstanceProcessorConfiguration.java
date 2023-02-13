package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.mapping.EgrunnervervArchiveInstanceMappingService;
import no.fintlabs.model.EgrunnervervArchiveInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<EgrunnervervArchiveInstance> archiveInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            EgrunnervervArchiveInstanceMappingService egrunnervervArchiveInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "archive",
                egrunnervervArchiveInstance -> Optional.ofNullable(egrunnervervArchiveInstance.getSys_id()),
                egrunnervervArchiveInstanceMappingService
        );
    }

}
