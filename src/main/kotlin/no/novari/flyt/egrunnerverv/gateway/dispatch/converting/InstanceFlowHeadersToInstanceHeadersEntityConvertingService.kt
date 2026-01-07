package no.novari.flyt.egrunnerverv.gateway.dispatch.converting

import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.springframework.stereotype.Service

@Service
class InstanceFlowHeadersToInstanceHeadersEntityConvertingService {
    fun convert(instanceFlowHeaders: InstanceFlowHeaders): InstanceHeadersEntity =
        InstanceHeadersEntity(
            sourceApplicationIntegrationId = instanceFlowHeaders.sourceApplicationIntegrationId,
            sourceApplicationInstanceId = instanceFlowHeaders.sourceApplicationInstanceId,
            archiveInstanceId = instanceFlowHeaders.archiveInstanceId,
        )
}
