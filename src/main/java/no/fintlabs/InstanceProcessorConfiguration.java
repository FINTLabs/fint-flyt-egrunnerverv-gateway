package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.mapping.EgrunnervervJournalpostInstanceMappingService;
import no.fintlabs.mapping.EgrunnervervSakInstanceMappingService;
import no.fintlabs.models.EgrunnervervJournalpostInstance;
import no.fintlabs.models.EgrunnervervSakInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<EgrunnervervSakInstance> sakInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            EgrunnervervSakInstanceMappingService egrunnervervSakInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "sak",
                egrunnervervSakInstance -> Optional.ofNullable(egrunnervervSakInstance.getSysId()),
                egrunnervervSakInstanceMappingService
        );
    }

    @Bean
    public InstanceProcessor<EgrunnervervJournalpostInstance> journalpostInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            EgrunnervervJournalpostInstanceMappingService egrunnervervJournalpostInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "journalpost",
                egrunnervervJournalpostInstance -> Optional.ofNullable(
                        egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSysId()
                ),
                egrunnervervJournalpostInstanceMappingService
        );
    }


}
