package no.novari.flyt.egrunnerverv.gateway.infrastructure.kafka.resources

import no.novari.fint.model.resource.FintLinks
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.flyt.cache.FlytCache
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil
import org.apache.kafka.common.TopicPartition
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AbstractConsumerSeekAware
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload

/**
 * Fills the resource caches from the entity topics. Every listener uses the base group id and reads its topic from the
 * beginning on assignment, as with fint-kafka 6 (`groupIdApplicationDefault()`, `seekToBeginningOnAssignment()`).
 */
@Configuration
class ResourceEntityConsumersConfiguration(
    private val administrativEnhetResourceCache: FlytCache<String, AdministrativEnhetResource>,
    private val arkivressursResourceCache: FlytCache<String, ArkivressursResource>,
    private val tilgangsrestriksjonResourceCache: FlytCache<String, TilgangsrestriksjonResource>,
    private val skjermingshjemmelResourceCache: FlytCache<String, SkjermingshjemmelResource>,
    private val journalStatusResourceCache: FlytCache<String, JournalStatusResource>,
    private val journalpostTypeResourceCache: FlytCache<String, JournalpostTypeResource>,
    private val personalressursResourceCache: FlytCache<String, PersonalressursResource>,
    private val personResourceCache: FlytCache<String, PersonResource>,
) : AbstractConsumerSeekAware() {
    @KafkaListener(topics = ["#{@administrativEnhetResourceTopic.name}"])
    fun onAdministrativEnhetResource(
        @Payload(required = false) resource: AdministrativEnhetResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = administrativEnhetResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@arkivressursResourceTopic.name}"])
    fun onArkivressursResource(
        @Payload(required = false) resource: ArkivressursResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = arkivressursResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@tilgangsrestriksjonResourceTopic.name}"])
    fun onTilgangsrestriksjonResource(
        @Payload(required = false) resource: TilgangsrestriksjonResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = tilgangsrestriksjonResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@skjermingshjemmelResourceTopic.name}"])
    fun onSkjermingshjemmelResource(
        @Payload(required = false) resource: SkjermingshjemmelResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = skjermingshjemmelResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@journalstatusResourceTopic.name}"])
    fun onJournalStatusResource(
        @Payload(required = false) resource: JournalStatusResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = journalStatusResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@journalposttypeResourceTopic.name}"])
    fun onJournalpostTypeResource(
        @Payload(required = false) resource: JournalpostTypeResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = journalpostTypeResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@personalressursResourceTopic.name}"])
    fun onPersonalressursResource(
        @Payload(required = false) resource: PersonalressursResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = personalressursResourceCache.update(key, resource)

    @KafkaListener(topics = ["#{@personResourceTopic.name}"])
    fun onPersonResource(
        @Payload(required = false) resource: PersonResource?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) = personResourceCache.update(key, resource)

    /**
     * A resource is cached under every self link and its record key (archive-gateway keys records with the first self
     * link). A new version and a tombstone find the previous version under the key, so no stale link is left behind.
     */
    private fun <R : FintLinks> FlytCache<String, R>.update(
        key: String?,
        resource: R?,
    ) {
        if (resource != null) {
            val links = ResourceLinkUtil.getSelfLinks(resource)
            if (key != null) {
                // Drops links the previous version had and this one has not, so a later tombstone leaves none behind.
                getOrNull(key)?.let { previous -> remove(ResourceLinkUtil.getSelfLinks(previous) - links.toSet()) }
            }
            put(listOfNotNull(key) + links, resource)
        } else if (key != null) {
            getOrNull(key)?.let { remove(ResourceLinkUtil.getSelfLinks(it)) }
            remove(key)
        }
    }

    override fun onPartitionsAssigned(
        assignments: Map<TopicPartition, Long>,
        callback: ConsumerSeekAware.ConsumerSeekCallback,
    ) {
        super.onPartitionsAssigned(assignments, callback)
        callback.seekToBeginning(assignments.keys)
    }
}
