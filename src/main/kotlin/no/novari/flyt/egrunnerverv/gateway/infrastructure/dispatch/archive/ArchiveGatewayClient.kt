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
 * Accepts `saksår/saksnummer` and compact mappe IDs with a four-digit year and five-digit case number.
 * Returns `null` for unsupported formats and on 404.
 */
@Service
class ArchiveGatewayClient(
    @param:Qualifier("archiveGatewayRestClient")
    private val restClient: RestClient,
) {
    fun getByMappeId(mappeId: String): SakResource? {
        val parts = COMPACT_MAPPE_ID.replace(mappeId, "$1/$2").split("/")
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

    private companion object {
        val COMPACT_MAPPE_ID = Regex("""\A([0-9]{4})([0-9]{5})\z""")
    }
}
