package no.novari.flyt.egrunnerverv.gateway.dispatch.converting;

import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowHeadersToInstanceHeadersEntityConvertingService {
    public InstanceHeadersEntity convert(InstanceFlowHeaders instanceFlowHeaders) {
        return InstanceHeadersEntity
                .builder()
                .sourceApplicationIntegrationId(instanceFlowHeaders.getSourceApplicationIntegrationId())
                .sourceApplicationInstanceId(instanceFlowHeaders.getSourceApplicationInstanceId())
                .archiveInstanceId(instanceFlowHeaders.getArchiveInstanceId())
                .build();
    }
}
