package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.kafka

import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.DispatchService
import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.kafka.starter.topic.EventCleanupFrequency
import no.novari.kafka.starter.topic.EventTopicConfig
import no.novari.kafka.starter.topic.EventTopicName
import no.novari.kafka.starter.topic.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.time.Duration

@Configuration
class InstanceConsumerConfiguration {
    @Bean
    fun instanceDispatchedTopic(topics: KafkaTopics): EventTopicName = topics.event("instance-dispatched")

    /** Created or updated at startup, as fint-kafka 6 did with `createOrModifyTopic`. */
    @Bean
    fun instanceDispatchedTopicCreation(
        topics: KafkaTopics,
        instanceDispatchedTopic: EventTopicName,
    ): NewTopic =
        topics.newTopic(
            instanceDispatchedTopic,
            EventTopicConfig(
                retention = Duration.ofDays(7),
                cleanupFrequency = EventCleanupFrequency.NORMAL,
                partitions = 1,
            ),
        )
}

/** Starts the dispatch of the receipt to ServiceNow. The records are tombstones, so only the header is read. */
@Component
class InstanceDispatchedListener(
    private val dispatchService: DispatchService,
) {
    @KafkaListener(topics = ["#{@instanceDispatchedTopic.name}"])
    fun on(
        @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
    ) = dispatchService.handleNewInstance(instanceFlowHeaders)
}
