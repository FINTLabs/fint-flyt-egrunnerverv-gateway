package no.fintlabs;

import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final EntityConsumerFactoryService entityConsumerFactoryService;
    private final ResourceRepository resourceRepository;


    public ResourceEntityConsumersConfiguration(EntityConsumerFactoryService entityConsumerFactoryService, ResourceRepository resourceRepository) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
        this.resourceRepository = resourceRepository;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ArkivressursResource> arkivressursResourceEntityConsumer(
    ) {
        return entityConsumerFactoryService.createFactory(
                ArkivressursResource.class,
                consumerRecord -> resourceRepository.updateArkivRessurs(consumerRecord.value())
        ).createContainer(EntityTopicNameParameters.builder().resource("arkiv.noark.arkivressurs").build());
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, PersonalressursResource> personalressursResourceEntityConsumer(
    ) {
        return entityConsumerFactoryService.createFactory(
                PersonalressursResource.class,
                consumerRecord -> resourceRepository.updatePersonalRessurs(consumerRecord.value())
        ).createContainer(EntityTopicNameParameters.builder().resource("administrasjon.personal.personalressurs").build());
    }

}
