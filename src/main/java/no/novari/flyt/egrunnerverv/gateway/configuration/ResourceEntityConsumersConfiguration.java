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
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import no.novari.kafka.requestreply.RequestListenerConfiguration;
import no.novari.kafka.requestreply.RequestListenerContainerFactory;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final RequestListenerContainerFactory requestListenerContainerFactory;
    private final ErrorHandlerFactory errorHandlerFactory;

    public ResourceEntityConsumersConfiguration(
            RequestListenerContainerFactory requestListenerContainerFactory,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        this.requestListenerContainerFactory = requestListenerContainerFactory;
        this.errorHandlerFactory = errorHandlerFactory;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        return requestListenerContainerFactory.createRecordConsumerFactory(
                resourceClass,
                Void.class,
                (ConsumerRecord<String, T> record) -> {
                    cache.put(ResourceLinkUtil.getSelfLinks(record.value()), record.value());
                    return ReplyProducerRecord.<Void>builder().build();
                },
                RequestListenerConfiguration
                        .stepBuilder(resourceClass)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
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
        return requestListenerContainerFactory.createRecordConsumerFactory(
                ArkivressursResource.class,
                Void.class,
                (ConsumerRecord<String, ArkivressursResource> record) -> {
                    arkivressursResourceCache.put(
                            ResourceLinkUtil.getSelfLinks(record.value()), record.value()
                    );
                    resourceRepository.updateArkivRessurs(record.value());
                    return ReplyProducerRecord.<Void>builder().build();
                },
                RequestListenerConfiguration
                        .stepBuilder(ArkivressursResource.class)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
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
        return requestListenerContainerFactory.createRecordConsumerFactory(
                PersonalressursResource.class,
                Void.class,
                (ConsumerRecord<String, PersonalressursResource> record) -> {
                    personalressursResourceCache.put(
                            ResourceLinkUtil.getSelfLinks(record.value()), record.value()
                    );
                    resourceRepository.updatePersonalRessurs(record.value());
                    return ReplyProducerRecord.<Void>builder().build();
                },
                RequestListenerConfiguration
                        .stepBuilder(PersonalressursResource.class)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
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
