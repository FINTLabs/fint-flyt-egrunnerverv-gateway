package no.novari.flyt.egrunnerverv.gateway

import no.novari.fint.model.resource.FintLinks
import no.novari.fint.model.resource.Link
import no.novari.flyt.egrunnerverv.gateway.exception.NoSuchLinkException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourceLinkUtilTest {
    private class Links(
        private val links: MutableMap<String, MutableList<Link>> = mutableMapOf(),
    ) : FintLinks {
        override fun getLinks(): MutableMap<String, MutableList<Link>> = links
    }

    @Test
    fun `getFirstSelfLink returns first self link`() {
        val links = Links()
        links.addSelf(Link("self-1"))
        links.addSelf(Link("self-2"))

        assertThat(ResourceLinkUtil.getFirstSelfLink(links)).isEqualTo("self-1")
        assertThat(ResourceLinkUtil.getSelfLinks(links)).containsExactly("self-1", "self-2")
    }

    @Test
    fun `getFirstSelfLink throws when there is no self link`() {
        val links = Links()

        assertThatThrownBy { ResourceLinkUtil.getFirstSelfLink(links) }
            .isInstanceOf(NoSuchLinkException::class.java)
            .hasMessageContaining("No self link")
    }

    @Test
    fun `getFirstLink returns first link from producer`() {
        val link = Link("href-1")
        assertThat(ResourceLinkUtil.getFirstLink { listOf(link) }).isEqualTo("href-1")
        assertThat(ResourceLinkUtil.getFirstLink { emptyList() }).isNull()
        assertThat(ResourceLinkUtil.getFirstLink { null }).isNull()
    }
}
