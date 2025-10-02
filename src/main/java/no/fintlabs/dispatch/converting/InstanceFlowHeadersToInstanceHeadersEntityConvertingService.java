package no.fintlabs.dispatch.converting;

import no.fintlabs.dispatch.model.InstanceHeadersEntity;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
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
