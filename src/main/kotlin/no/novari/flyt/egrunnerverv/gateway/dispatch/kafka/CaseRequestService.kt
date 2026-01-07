package no.novari.flyt.egrunnerverv.gateway.dispatch.kafka

import no.fint.model.resource.arkiv.noark.SakResource
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.requestreply.RequestProducerRecord
import no.novari.kafka.requestreply.RequestTemplate
import no.novari.kafka.requestreply.RequestTemplateFactory
import no.novari.kafka.requestreply.topic.ReplyTopicService
import no.novari.kafka.requestreply.topic.configuration.ReplyTopicConfiguration
import no.novari.kafka.requestreply.topic.name.ReplyTopicNameParameters
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CaseRequestService(
    @Value("\${novari.kafka.application-id}") applicationId: String,
    replyTopicService: ReplyTopicService,
    requestTemplateFactory: RequestTemplateFactory,
) {
    private val template: RequestTemplate<String, SakResource>
    private val requestTopicNameParameters =
        RequestTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgIdApplicationDefault()
                    .domainContextApplicationDefault()
                    .build(),
            ).resourceName("arkiv-noark-sak")
            .parameterName("mappeid")
            .build()

    init {

        val replyTopicNameParameters =
            ReplyTopicNameParameters
                .builder()
                .applicationId(applicationId)
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).resourceName("arkiv-noark-sak")
                .build()

        replyTopicService.createOrModifyTopic(
            replyTopicNameParameters,
            ReplyTopicConfiguration
                .builder()
                .retentionTime(RETENTION_TIME)
                .build(),
        )

        template =
            requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                String::class.java,
                SakResource::class.java,
                Duration.ofSeconds(60),
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
            )
    }

    fun getByMappeId(mappeId: String): SakResource? =
        template
            .requestAndReceive(
                RequestProducerRecord
                    .builder<String>()
                    .topicNameParameters(requestTopicNameParameters)
                    .value(mappeId)
                    .build(),
            ).value()

    companion object {
        private val RETENTION_TIME = Duration.ofMinutes(10)
    }
}
