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
 * Looks up the original archive instance ID. Archive-gateway filters journalposts when the ID includes journalpost numbers.
 * Returns `null` on 404.
 */
@Service
class ArchiveGatewayClient(
    @param:Qualifier("archiveGatewayRestClient")
    private val restClient: RestClient,
) {
    fun getByArchiveInstanceId(archiveInstanceId: String): SakResource? =
        try {
            restClient
                .get()
                .uri("/arkiv/saker?archiveInstanceId={archiveInstanceId}", archiveInstanceId)
                .retrieve()
                .body<SakResource>()
        } catch (_: HttpClientErrorException.NotFound) {
            null
        }
}
