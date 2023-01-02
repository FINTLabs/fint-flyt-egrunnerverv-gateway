package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.mapping.EgrunnervervArchiveInstanceMappingService;
import no.fintlabs.mapping.EgrunnervervDocumentInstanceMappingService;
import no.fintlabs.model.EgrunnervervArchiveInstance;
import no.fintlabs.model.EgrunnervervDocumentInstance;
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

    @Bean
    public InstanceProcessor<EgrunnervervDocumentInstance> documentInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            EgrunnervervDocumentInstanceMappingService egrunnervervDocumentInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "document",
                egrunnervervDocumentInstance -> Optional.ofNullable(egrunnervervDocumentInstance.getTittel()),
                egrunnervervDocumentInstanceMappingService
        );
    }

}
