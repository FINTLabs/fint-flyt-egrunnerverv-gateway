package no.fintlabs.instance.configuration;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.instance.mapping.EgrunnervervJournalpostInstanceMappingService;
import no.fintlabs.instance.mapping.EgrunnervervSakInstanceMappingService;
import no.fintlabs.instance.model.EgrunnervervJournalpostInstance;
import no.fintlabs.instance.model.EgrunnervervSakInstance;
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
