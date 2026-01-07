package no.novari.flyt.egrunnerverv.gateway.configuration

import jakarta.annotation.Nullable
import no.fint.model.resource.FintLinks
import no.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.fint.model.resource.arkiv.noark.ArkivressursResource
import no.fint.model.resource.felles.PersonResource
import no.novari.cache.FintCache
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.util.function.Consumer

@Configuration
class ResourceEntityConsumersConfiguration(
    private val listenerFactoryService: ParameterizedListenerContainerFactoryService,
    private val errorHandlerFactory: ErrorHandlerFactory,
) {
    private fun <T : FintLinks> createCacheConsumer(
        resourceName: String,
        resourceClass: Class<T>,
        cache: FintCache<String, T>,
        @Nullable afterCache: Consumer<T>?,
    ): ConcurrentMessageListenerContainer<String, T> {
        val listenerConfig =
            ListenerConfiguration
                .stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .seekToBeginningOnAssignment()
                .build()

        val errorHandler =
            errorHandlerFactory.createErrorHandler(
                ErrorHandlerConfiguration
                    .stepBuilder<T>()
                    .noRetries()
                    .skipFailedRecords()
                    .build(),
            )

        return listenerFactoryService
            .createRecordListenerContainerFactory<T>(
                resourceClass,
                { record: ConsumerRecord<String, T> ->
                    val value = record.value()
                    cache.put(ResourceLinkUtil.getSelfLinks(value), value)
                    afterCache?.accept(value)
                },
                listenerConfig,
                errorHandler,
            ).createContainer(
                EntityTopicNameParameters
                    .builder()
                    .topicNamePrefixParameters(
                        TopicNamePrefixParameters
                            .stepBuilder()
                            .orgIdApplicationDefault()
                            .domainContextApplicationDefault()
                            .build(),
                    ).resourceName(resourceName)
                    .build(),
            )
    }

    @Bean
    fun administrativEnhetResourceEntityConsumer(
        administrativEnhetResourceCache: FintCache<String, AdministrativEnhetResource>,
    ): ConcurrentMessageListenerContainer<String, AdministrativEnhetResource> =
        createCacheConsumer(
            resourceName = "arkiv-noark-administrativenhet",
            resourceClass = AdministrativEnhetResource::class.java,
            cache = administrativEnhetResourceCache,
            afterCache = null,
        )

    @Bean
    fun arkivressursResourceEntityConsumer(
        arkivressursResourceCache: FintCache<String, ArkivressursResource>,
        resourceRepository: ResourceRepository,
    ): ConcurrentMessageListenerContainer<String, ArkivressursResource> =
        createCacheConsumer(
            resourceName = "arkiv-noark-arkivressurs",
            resourceClass = ArkivressursResource::class.java,
            cache = arkivressursResourceCache,
            afterCache = resourceRepository::updateArkivRessurs,
        )

    @Bean
    fun tilgangsrestriksjonResourceEntityConsumer(
        tilgangsrestriksjonResourceCache: FintCache<String, TilgangsrestriksjonResource>,
    ): ConcurrentMessageListenerContainer<String, TilgangsrestriksjonResource> =
        createCacheConsumer(
            resourceName = "arkiv-kodeverk-tilgangsrestriksjon",
            resourceClass = TilgangsrestriksjonResource::class.java,
            cache = tilgangsrestriksjonResourceCache,
            afterCache = null,
        )

    @Bean
    fun skjermingshjemmelResourceEntityConsumer(
        skjermingshjemmelResourceCache: FintCache<String, SkjermingshjemmelResource>,
    ): ConcurrentMessageListenerContainer<String, SkjermingshjemmelResource> =
        createCacheConsumer(
            resourceName = "arkiv-kodeverk-skjermingshjemmel",
            resourceClass = SkjermingshjemmelResource::class.java,
            cache = skjermingshjemmelResourceCache,
            afterCache = null,
        )

    @Bean
    fun journalstatusResourceEntityConsumer(
        journalStatusResourceCache: FintCache<String, JournalStatusResource>,
    ): ConcurrentMessageListenerContainer<String, JournalStatusResource> =
        createCacheConsumer(
            resourceName = "arkiv-kodeverk-journalstatus",
            resourceClass = JournalStatusResource::class.java,
            cache = journalStatusResourceCache,
            afterCache = null,
        )

    @Bean
    fun journalposttypeEntityConsumer(
        journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource>,
    ): ConcurrentMessageListenerContainer<String, JournalpostTypeResource> =
        createCacheConsumer(
            resourceName = "arkiv-kodeverk-journalposttype",
            resourceClass = JournalpostTypeResource::class.java,
            cache = journalpostTypeResourceCache,
            afterCache = null,
        )

    @Bean
    fun personalressursResourceEntityConsumer(
        personalressursResourceCache: FintCache<String, PersonalressursResource>,
        resourceRepository: ResourceRepository,
    ): ConcurrentMessageListenerContainer<String, PersonalressursResource> =
        createCacheConsumer(
            resourceName = "administrasjon-personal-personalressurs",
            resourceClass = PersonalressursResource::class.java,
            cache = personalressursResourceCache,
            afterCache = resourceRepository::updatePersonalRessurs,
        )

    @Bean
    fun personResourceEntityConsumer(
        personResourceCache: FintCache<String, PersonResource>,
    ): ConcurrentMessageListenerContainer<String, PersonResource> =
        createCacheConsumer(
            resourceName = "administrasjon-personal-person",
            resourceClass = PersonResource::class.java,
            cache = personResourceCache,
            afterCache = null,
        )
}
