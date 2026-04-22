package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.springframework.stereotype.Service

@Service
class InstanceFlowHeadersToInstanceHeadersEntityConvertingService {
    fun convert(instanceFlowHeaders: InstanceFlowHeaders): InstanceHeadersEntity =
        InstanceHeadersEntity(
            sourceApplicationInstanceId =
                instanceFlowHeaders.sourceApplicationInstanceId
                    ?: error("Missing sourceApplicationInstanceId"),
            sourceApplicationIntegrationId =
                instanceFlowHeaders.sourceApplicationIntegrationId
                    ?: error("Missing sourceApplicationIntegrationId"),
            archiveInstanceId = instanceFlowHeaders.archiveInstanceId.orEmpty(),
        )
}
