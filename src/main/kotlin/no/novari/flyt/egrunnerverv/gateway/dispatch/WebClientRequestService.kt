package no.novari.flyt.egrunnerverv.gateway.dispatch

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class WebClientRequestService(
    @param:Qualifier("dispatchRestClient") private val restClient: RestClient,
) {
    private val objectMapper = ObjectMapper()

    fun dispatchInstance(instanceReceiptDispatchEntity: InstanceReceiptDispatchEntity): InstanceReceiptDispatchEntity {
        val classType =
            instanceReceiptDispatchEntity.classType
                ?: throw IllegalStateException(
                    "Missing classType for ${instanceReceiptDispatchEntity.sourceApplicationInstanceId}",
                )

        val instanceToDispatch =
            try {
                objectMapper.readValue(
                    instanceReceiptDispatchEntity.instanceReceipt,
                    classType,
                )
            } catch (e: JsonProcessingException) {
                throw e
            }

        try {
            restClient
                .patch()
                .uri(instanceReceiptDispatchEntity.uri)
                .body(instanceToDispatch)
                .retrieve()
                .toBodilessEntity()
        } catch (ex: RestClientResponseException) {
            throw ex
        }

        return instanceReceiptDispatchEntity
    }
}
