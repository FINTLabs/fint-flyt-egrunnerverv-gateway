package no.novari.flyt.egrunnerverv.gateway.configuration;

import jakarta.annotation.Nullable;
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
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.function.Consumer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final ParameterizedListenerContainerFactoryService listenerFactoryService;
    private final ErrorHandlerFactory errorHandlerFactory;

    public ResourceEntityConsumersConfiguration(
            ParameterizedListenerContainerFactoryService listenerFactoryService,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        this.listenerFactoryService = listenerFactoryService;
        this.errorHandlerFactory = errorHandlerFactory;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceName,
            Class<T> resourceClass,
            FintCache<String, T> cache,
            @Nullable Consumer<T> afterCache
    ) {
        ListenerConfiguration listenerConfig = ListenerConfiguration.stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .seekToBeginningOnAssignment()
                .build();

        return listenerFactoryService
                .createRecordListenerContainerFactory(
                        resourceClass,
                        record -> {
                            T value = record.value();
                            cache.put(ResourceLinkUtil.getSelfLinks(value), value);
                            if (afterCache != null) {
                                afterCache.accept(value);
                            }
                        },
                        listenerConfig,
                        errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration.<T>builder().build())
                )
                .createContainer(
                        EntityTopicNameParameters.builder()
                                .topicNamePrefixParameters(
                                        TopicNamePrefixParameters.stepBuilder()
                                                .orgIdApplicationDefault()
                                                .domainContextApplicationDefault()
                                                .build()
                                )
                                .resourceName(resourceName)
                                .build()
                );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, AdministrativEnhetResource> administrativEnhetResourceEntityConsumer(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-noark-administrativenhet",
                AdministrativEnhetResource.class,
                administrativEnhetResourceCache,
                null
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ArkivressursResource> arkivressursResourceEntityConsumer(
            FintCache<String, ArkivressursResource> arkivressursResourceCache,
            ResourceRepository resourceRepository
    ) {
        return createCacheConsumer(
                "arkiv-noark-arkivressurs",
                ArkivressursResource.class,
                arkivressursResourceCache,
                resourceRepository::updateArkivRessurs
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceEntityConsumer(
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-tilgangsrestriksjon",
                TilgangsrestriksjonResource.class,
                tilgangsrestriksjonResourceCache,
                null
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, SkjermingshjemmelResource> skjermingshjemmelResourceEntityConsumer(
            FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-skjermingshjemmel",
                SkjermingshjemmelResource.class,
                skjermingshjemmelResourceCache,
                null
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, JournalStatusResource> journalstatusResourceEntityConsumer(
            FintCache<String, JournalStatusResource> journalStatusResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-journalstatus",
                JournalStatusResource.class,
                journalStatusResourceCache,
                null
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, JournalpostTypeResource> journalposttypeEntityConsumer(
            FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-journalposttype",
                JournalpostTypeResource.class,
                journalpostTypeResourceCache,
                null
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonalressursResource> personalressursResourceEntityConsumer(
            FintCache<String, PersonalressursResource> personalressursResourceCache,
            ResourceRepository resourceRepository
    ) {
        return createCacheConsumer(
                "administrasjon-personal-personalressurs",
                PersonalressursResource.class,
                personalressursResourceCache,
                resourceRepository::updatePersonalRessurs
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonResource> personResourceEntityConsumer(
            FintCache<String, PersonResource> personResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-personal-person",
                PersonResource.class,
                personResourceCache,
                null
        );
    }
}
