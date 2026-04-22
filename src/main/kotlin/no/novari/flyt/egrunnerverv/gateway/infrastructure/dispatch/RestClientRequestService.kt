package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceReceiptDispatchEntity
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class RestClientRequestService(
    private val restClient: RestClient,
    private val objectMapper: ObjectMapper,
) {
    fun dispatchInstance(instanceReceiptDispatchEntity: InstanceReceiptDispatchEntity) {
        val uri = instanceReceiptDispatchEntity.uri
        val payload = objectMapper.readTree(instanceReceiptDispatchEntity.instanceReceipt)

        log.atInfo {
            message = "Dispatching instance receipt"
            arguments = arrayOf(kv("uri", uri))
        }

        try {
            val response =
                restClient
                    .patch()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity()

            log.atInfo {
                message = "Successfully dispatched instance receipt"
                arguments = arrayOf(kv("uri", uri), kv("statusCode", response.statusCode))
            }
        } catch (exception: RestClientResponseException) {
            log.atError {
                cause = exception
                message = "Failed to dispatch instance receipt"
                arguments = arrayOf(uri, exception.statusCode, exception.responseBodyAsString)
            }
            throw exception
        } catch (exception: Exception) {
            log.atError {
                cause = exception
                message = "Failed to dispatch instance receipt"
                arguments = arrayOf(uri, exception.message)
            }
            throw exception
        }
    }

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}
