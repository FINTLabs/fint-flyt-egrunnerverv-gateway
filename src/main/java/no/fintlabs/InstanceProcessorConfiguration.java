package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.mapping.EgrunnervervArchiveInstanceMappingService;
import no.fintlabs.model.EgrunnervervArchiveInstanceToMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<EgrunnervervArchiveInstanceToMap> archiveInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            EgrunnervervArchiveInstanceMappingService egrunnervervArchiveInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "archive",
                egrunnervervArchiveInstanceToMap -> Optional.ofNullable(egrunnervervArchiveInstanceToMap.getSysId()),
                egrunnervervArchiveInstanceMappingService
        );
    }

}
