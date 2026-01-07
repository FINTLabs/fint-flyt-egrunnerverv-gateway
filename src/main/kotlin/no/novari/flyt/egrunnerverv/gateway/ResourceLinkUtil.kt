package no.novari.flyt.egrunnerverv.gateway

import no.fint.model.resource.FintLinks
import no.fint.model.resource.Link
import no.novari.flyt.egrunnerverv.gateway.exception.NoSuchLinkException

object ResourceLinkUtil {
    fun getFirstSelfLink(resource: FintLinks): String =
        resource.selfLinks
            .firstOrNull()
            ?.href
            ?: throw NoSuchLinkException.noSelfLink(resource)

    fun getSelfLinks(resource: FintLinks): List<String> = resource.selfLinks.map(Link::getHref)

    fun getOptionalFirstLink(linkProducer: () -> List<Link>?): String? = linkProducer.invoke()?.firstOrNull()?.href
}
