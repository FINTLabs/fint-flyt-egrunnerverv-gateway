package no.novari.flyt.egrunnerverv.gateway.infrastructure.kafka.resources

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import org.apache.kafka.common.TopicPartition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.kafka.listener.ConsumerSeekAware
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

class ResourceEntityConsumersTest {
    private val cache = ResourceEntityCacheConfiguration().arkivressursResourceCache()
    private val listeners =
        ResourceEntityConsumersConfiguration(mock(), cache, mock(), mock(), mock(), mock(), mock(), mock())

    @Test
    fun `Jackson 3 resource is cached by every self link and a tombstone removes all of them`() {
        val original =
            ArkivressursResource().apply {
                addSelf(Link.with("https://fint.test/arkivressurs/1"))
                addSelf(Link.with("https://fint.test/arkivressurs/B"))
            }
        val mapper = jsonMapper { addModule(kotlinModule()) }
        val resource = mapper.readValue(mapper.writeValueAsString(original), ArkivressursResource::class.java)

        listeners.onArkivressursResource(resource, "https://fint.test/arkivressurs/1")
        assertThat(cache.get("https://fint.test/arkivressurs/1")).isSameAs(resource)
        assertThat(cache.get("https://fint.test/arkivressurs/B")).isSameAs(resource)

        listeners.onArkivressursResource(null, "https://fint.test/arkivressurs/1")
        assertThat(cache.getOrNull("https://fint.test/arkivressurs/1")).isNull()
        assertThat(cache.getOrNull("https://fint.test/arkivressurs/B")).isNull()
    }

    @Test
    fun `record without a key is cached, and a tombstone without a key is ignored`() {
        val resource = ArkivressursResource().apply { addSelf(Link.with("https://fint.test/arkivressurs/2")) }
        listeners.onArkivressursResource(resource, null)
        assertThat(cache.get("https://fint.test/arkivressurs/2")).isSameAs(resource)

        listeners.onArkivressursResource(null, null)
        assertThat(cache.get("https://fint.test/arkivressurs/2")).isSameAs(resource)
    }

    @Test
    fun `assignment rebuilds caches from the beginning`() {
        val callback = mock<ConsumerSeekAware.ConsumerSeekCallback>()
        val assignments = mapOf(TopicPartition("fintlabs-no.flyt.entity.arkiv-noark-arkivressurs", 0) to 42L)
        listeners.onPartitionsAssigned(assignments, callback)
        verify(callback).seekToBeginning(assignments.keys)
    }
}
