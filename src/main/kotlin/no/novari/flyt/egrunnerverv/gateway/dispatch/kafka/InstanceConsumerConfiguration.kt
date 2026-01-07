package no.novari.flyt.egrunnerverv.gateway.dispatch.kafka

import no.novari.flyt.egrunnerverv.gateway.dispatch.DispatchService
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.EventTopicService
import no.novari.kafka.topic.configuration.EventCleanupFrequency
import no.novari.kafka.topic.configuration.EventTopicConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.time.Duration

@Configuration
class InstanceConsumerConfiguration(
    private val eventTopicService: EventTopicService,
    private val dispatchService: DispatchService,
) {
    @Bean
    fun prepareInstanceToDispatchEventConsumer(
        instanceFlowListenerFactoryService: InstanceFlowListenerFactoryService,
        errorHandlerFactory: ErrorHandlerFactory,
    ): ConcurrentMessageListenerContainer<String, InstanceReceiptDispatchEntity> {
        val topic =
            EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).eventName("instance-dispatched")
                .build()

        eventTopicService.createOrModifyTopic(
            topic,
            EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(RETENTION_TIME)
                .cleanupFrequency(CLEANUP_FREQUENCY)
                .build(),
        )

        val errorHandler =
            errorHandlerFactory.createErrorHandler(
                ErrorHandlerConfiguration
                    .stepBuilder<InstanceReceiptDispatchEntity>()
                    .noRetries()
                    .skipFailedRecords()
                    .build(),
            )

        return instanceFlowListenerFactoryService
            .createRecordListenerContainerFactory(
                InstanceReceiptDispatchEntity::class.java,
                { record: InstanceFlowConsumerRecord<InstanceReceiptDispatchEntity> ->
                    dispatchService.handleNewInstance(record.instanceFlowHeaders)
                },
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
                errorHandler,
            ).createContainer(topic)
    }

    companion object {
        private const val PARTITIONS = 1
        private val RETENTION_TIME = Duration.ofDays(7)
        private val CLEANUP_FREQUENCY = EventCleanupFrequency.NORMAL
    }
}
