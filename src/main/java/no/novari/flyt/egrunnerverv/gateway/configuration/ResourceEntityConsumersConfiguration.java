package no.novari.flyt.egrunnerverv.gateway.configuration;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource;
import no.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource;
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.novari.cache.FintCache;
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil;
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService;
    private final ErrorHandlerFactory errorHandlerFactory;

    public ResourceEntityConsumersConfiguration(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        this.parameterizedListenerContainerFactoryService = parameterizedListenerContainerFactoryService;
        this.errorHandlerFactory = errorHandlerFactory;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                resourceClass,
                (ConsumerRecord<String, T> record) -> cache.put(
                        ResourceLinkUtil.getSelfLinks(record.value()), record.value()
                ),
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .seekToBeginningOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build()
                )
        ).createContainer(EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName(resourceReference)
                .build());
    }

    @Bean
    ConcurrentMessageListenerContainer<String, AdministrativEnhetResource> administrativEnhetResourceEntityConsumer(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-noark-administrativenhet",
                AdministrativEnhetResource.class,
                administrativEnhetResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ArkivressursResource> arkivressursResourceEntityConsumer(
            FintCache<String, ArkivressursResource> arkivressursResourceCache,
            ResourceRepository resourceRepository
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                ArkivressursResource.class,
                (ConsumerRecord<String, ArkivressursResource> record) -> {
                    arkivressursResourceCache.put(
                            ResourceLinkUtil.getSelfLinks(record.value()), record.value()
                    );
                    resourceRepository.updateArkivRessurs(record.value());
                },
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .seekToBeginningOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build()
                )
        ).createContainer(EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("arkiv-noark-arkivressurs")
                .build()
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceEntityConsumer(
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-tilgangsrestriksjon",
                TilgangsrestriksjonResource.class,
                tilgangsrestriksjonResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, SkjermingshjemmelResource> skjermingshjemmelResourceEntityConsumer(
            FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-skjermingshjemmel",
                SkjermingshjemmelResource.class,
                skjermingshjemmelResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, JournalStatusResource> journalstatusResourceEntityConsumer(
            FintCache<String, JournalStatusResource> journalStatusResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-journalstatus",
                JournalStatusResource.class,
                journalStatusResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, JournalpostTypeResource> journalposttypeEntityConsumer(
            FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-journalposttype",
                JournalpostTypeResource.class,
                journalpostTypeResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonalressursResource> personalressursResourceEntityConsumer(
            FintCache<String, PersonalressursResource> personalressursResourceCache,
            ResourceRepository resourceRepository
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                PersonalressursResource.class,
                (ConsumerRecord<String, PersonalressursResource> record) -> {
                    personalressursResourceCache.put(
                            ResourceLinkUtil.getSelfLinks(record.value()), record.value()
                    );
                    resourceRepository.updatePersonalRessurs(record.value());
                },
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .seekToBeginningOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build()
                )
        ).createContainer(EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("administrasjon-personal-personalressurs")
                .build()
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonResource> personResourceEntityConsumer(
            FintCache<String, PersonResource> personResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-personal-person",
                PersonResource.class,
                personResourceCache
        );
    }

}
