package no.fintlabs;

import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.models.EgrunnervervSimpleSakInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EgrunnervervSimpleSakProducerService {
    @Value("${fint.flyt.egrunnerverv.retentionTimeInDays:30}")
    private Long retentionTimeInDays;

    private final EventProducer<EgrunnervervSimpleSakInstance> simpleSakInstanceEventProducer;

    private final EventTopicNameParameters topicNameParameters;

    public EgrunnervervSimpleSakProducerService(
            EventProducerFactory eventProducerFactory,
            EventTopicService eventTopicService) {
        this.simpleSakInstanceEventProducer = eventProducerFactory.createProducer(EgrunnervervSimpleSakInstance.class);
        this.topicNameParameters = EventTopicNameParameters.builder()
                .eventName("egrunnerverv-sak-instance")
                .build();
        if (retentionTimeInDays != null) {
            eventTopicService.ensureTopic(topicNameParameters, Duration.ofDays(retentionTimeInDays).toMillis());
        }
    }

    public void publishSimpleSakInstance(EgrunnervervSimpleSakInstance egrunnervervSimpleSakInstance) {
        simpleSakInstanceEventProducer.send(
                EventProducerRecord.<EgrunnervervSimpleSakInstance>builder()
                        .topicNameParameters(topicNameParameters)
                        .value(egrunnervervSimpleSakInstance)
                        .build()
        );
    }

}

