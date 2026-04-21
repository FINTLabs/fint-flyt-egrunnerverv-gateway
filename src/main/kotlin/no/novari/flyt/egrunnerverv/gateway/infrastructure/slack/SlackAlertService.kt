package no.novari.flyt.egrunnerverv.gateway.infrastructure.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.net.URI

@Service
class SlackAlertService(
    @param:Qualifier("slackRestClient")
    private val restClient: RestClient,
    @param:Value("\${fint.org-id}")
    private val orgId: String,
    @param:Value("\${fint.application-id}")
    private val applicationId: String,
    @param:Value("\${slack.webhook.url:}")
    private val slackWebhookUrl: String,
) {
    fun sendMessage(text: String) {
        if (slackWebhookUrl.isBlank()) {
            log.debug { "Skipping Slack alert because webhook URL is blank" }
            return
        }

        try {
            restClient
                .post()
                .uri(URI.create(slackWebhookUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("text" to formatMessageWithPrefix(text)))
                .retrieve()
                .toBodilessEntity()
        } catch (exception: RestClientException) {
            log.atError {
                cause = exception
                message = "Failed to send Slack alert"
            }
        }
    }

    private fun formatMessageWithPrefix(message: String): String = "$orgId-$applicationId-$message"

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}
