package no.novari.flyt.egrunnerverv.gateway.instance.configuration;

import no.novari.flyt.egrunnerverv.gateway.instance.mapping.EgrunnervervJournalpostInstanceMappingService;
import no.novari.flyt.egrunnerverv.gateway.instance.mapping.EgrunnervervSakInstanceMappingService;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance;
import no.novari.flyt.instance.gateway.InstanceProcessor;
import no.novari.flyt.instance.gateway.InstanceProcessorFactoryService;
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
