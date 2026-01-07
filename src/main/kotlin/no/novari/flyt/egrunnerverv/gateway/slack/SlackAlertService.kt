package no.novari.flyt.egrunnerverv.gateway.slack

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class SlackAlertService(
    @param:Value("\${fint.org-id}") private val orgId: String,
    @param:Value("\${fint.application-id}") private val applicationId: String,
    @param:Value("\${slack.webhook.url}") private val slackWebhookUrl: String,
    @param:Qualifier("slackRestClient") private val restClient: RestClient,
) {
    fun sendMessage(message: String) {
        val payload = mapOf("text" to formatMessageWithPrefix(message))
        try {
            restClient
                .post()
                .uri(slackWebhookUrl)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        } catch (ex: RestClientResponseException) {
            logger.warn("Slack webhook failed with status={}", ex.statusCode, ex)
        } catch (ex: RuntimeException) {
            logger.warn("Slack webhook failed", ex)
        }
    }

    private fun formatMessageWithPrefix(message: String): String = "$orgId-$applicationId-$message"

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(SlackAlertService::class.java)
    }
}
