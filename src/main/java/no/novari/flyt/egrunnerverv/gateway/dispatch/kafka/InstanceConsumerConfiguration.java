package no.novari.flyt.egrunnerverv.gateway.dispatch.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.egrunnerverv.gateway.dispatch.DispatchService;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.topic.EventTopicService;
import no.novari.kafka.topic.configuration.EventCleanupFrequency;
import no.novari.kafka.topic.configuration.EventTopicConfiguration;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;

@Configuration
@Slf4j
public class InstanceConsumerConfiguration {

    private final EventTopicService eventTopicService;
    private final DispatchService dispatchService;

    private static final int PARTITIONS = 1;
    private static final Duration RETENTION_TIME = Duration.ofDays(4);
    private static final EventCleanupFrequency CLEANUP_FREQUENCY = EventCleanupFrequency.NORMAL;

    public InstanceConsumerConfiguration(
            EventTopicService eventTopicService,
            DispatchService dispatchService) {
        this.eventTopicService = eventTopicService;
        this.dispatchService = dispatchService;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceReceiptDispatchEntity>
    prepareInstanceToDispatchEventConsumer(
            InstanceFlowListenerFactoryService instanceFlowListenerFactoryService,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        EventTopicNameParameters topic = EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .eventName("instance-dispatched")
                .build();

        eventTopicService.createOrModifyTopic(topic, EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(RETENTION_TIME)
                .cleanupFrequency(CLEANUP_FREQUENCY)
                .build()
        );

        return instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
                InstanceReceiptDispatchEntity.class,
                instanceFlowConsumerRecord -> dispatchService.handleNewInstance(
                        instanceFlowConsumerRecord.getInstanceFlowHeaders()
                ),
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build()
                )
        ).createContainer(topic);
    }

}
