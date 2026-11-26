package no.novari.flyt.egrunnerverv.gateway.infrastructure.kafka.resources

import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.flyt.cache.FlytCache
import no.novari.kafka.starter.topic.KafkaTopics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import java.util.Locale

@Configuration
class ResourceEntityCacheConfiguration {
    /** As in fint-flyt-kafka 7 (`noRetries().skipFailedRecords()`): a failed record is logged and skipped. */
    @Bean
    fun kafkaErrorHandler(): CommonErrorHandler = DefaultErrorHandler(FixedBackOff(0, 0))

    @Bean
    fun administrativEnhetResourceTopic(topics: KafkaTopics) = topics.entity("arkiv-noark-administrativenhet")

    @Bean
    fun arkivressursResourceTopic(topics: KafkaTopics) = topics.entity("arkiv-noark-arkivressurs")

    @Bean
    fun tilgangsrestriksjonResourceTopic(topics: KafkaTopics) = topics.entity("arkiv-kodeverk-tilgangsrestriksjon")

    @Bean
    fun skjermingshjemmelResourceTopic(topics: KafkaTopics) = topics.entity("arkiv-kodeverk-skjermingshjemmel")

    @Bean
    fun journalstatusResourceTopic(topics: KafkaTopics) = topics.entity("arkiv-kodeverk-journalstatus")

    @Bean
    fun journalposttypeResourceTopic(topics: KafkaTopics) = topics.entity("arkiv-kodeverk-journalposttype")

    @Bean
    fun personalressursResourceTopic(topics: KafkaTopics) = topics.entity("administrasjon-personal-personalressurs")

    @Bean
    fun personResourceTopic(topics: KafkaTopics) = topics.entity("administrasjon-personal-person")

    @Bean
    fun administrativEnhetResourceCache(): FlytCache<String, AdministrativEnhetResource> =
        createCache(AdministrativEnhetResource::class.java)

    @Bean
    fun arkivressursResourceCache(): FlytCache<String, ArkivressursResource> =
        createCache(ArkivressursResource::class.java)

    @Bean
    fun tilgangsrestriksjonResourceCache(): FlytCache<String, TilgangsrestriksjonResource> =
        createCache(TilgangsrestriksjonResource::class.java)

    @Bean
    fun skjermingshjemmelResourceCache(): FlytCache<String, SkjermingshjemmelResource> =
        createCache(SkjermingshjemmelResource::class.java)

    @Bean
    fun journalstatusResourceCache(): FlytCache<String, JournalStatusResource> =
        createCache(JournalStatusResource::class.java)

    @Bean
    fun journalposttypeResourceCache(): FlytCache<String, JournalpostTypeResource> =
        createCache(JournalpostTypeResource::class.java)

    @Bean
    fun personalressursResourceCache(): FlytCache<String, PersonalressursResource> =
        createCache(PersonalressursResource::class.java)

    @Bean
    fun personResourceCache(): FlytCache<String, PersonResource> = createCache(PersonResource::class.java)

    // The same alias as with flyt-cache 3, so the metrics keep their cache tag.
    private fun <V : Any> createCache(resourceClass: Class<V>): FlytCache<String, V> =
        FlytCache(resourceClass.name.lowercase(Locale.ROOT))
}
