package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.archive

import no.novari.fint.model.resource.arkiv.noark.SakResource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * Client for the internal client API in fint-flyt-archive-gateway.
 *
 * Returns `null` on 404, and for a mappeId that is not on the format `saksår/saksnummer`, which archive-gateway
 * could not find either.
 */
@Service
class ArchiveGatewayClient(
    @param:Qualifier("archiveGatewayRestClient")
    private val restClient: RestClient,
) {
    fun getByMappeId(mappeId: String): SakResource? {
        val parts = mappeId.split("/")
        if (parts.size != 2) {
            return null
        }
        val (caseYear, caseNumber) = parts
        return try {
            restClient
                .get()
                .uri("/arkiv/saker/{caseYear}/{caseNumber}", caseYear, caseNumber)
                .retrieve()
                .body<SakResource>()
        } catch (_: HttpClientErrorException.NotFound) {
            null
        }
    }
}
