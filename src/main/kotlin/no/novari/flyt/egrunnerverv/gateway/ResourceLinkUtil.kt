package no.novari.flyt.egrunnerverv.gateway

import no.novari.fint.model.resource.FintLinks
import no.novari.fint.model.resource.Link
import no.novari.flyt.egrunnerverv.gateway.exception.NoSuchLinkException

object ResourceLinkUtil {
    fun getFirstSelfLink(resource: FintLinks): String =
        run {
            val selfLinks: List<Link>? = resource.selfLinks
            selfLinks
                ?.firstOrNull()
                ?.href
                ?: throw NoSuchLinkException.noSelfLink(resource)
        }

    fun getSelfLinks(resource: FintLinks): List<String> {
        val selfLinks: List<Link>? = resource.selfLinks
        return selfLinks?.map(Link::getHref).orEmpty()
    }

    fun getFirstLink(linkProducer: () -> List<Link>?): String? = linkProducer()?.firstOrNull()?.href
}
