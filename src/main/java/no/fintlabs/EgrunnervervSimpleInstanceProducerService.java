package no.fintlabs;

import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.models.EgrunnervervSimpleInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EgrunnervervSimpleInstanceProducerService {

    private final EventProducer<EgrunnervervSimpleInstance> simpleSakInstanceEventProducer;

    private final EventTopicNameParameters sakTopicNameParameters;
    private final EventTopicNameParameters journalpostTopicNameParameters;

    public EgrunnervervSimpleInstanceProducerService(
            @Value("${fint.flyt.egrunnerverv.retentionTimeInDays:30}") Long retentionTimeInDays,
            EventProducerFactory eventProducerFactory,
            EventTopicService eventTopicService) {
        this.simpleSakInstanceEventProducer = eventProducerFactory.createProducer(EgrunnervervSimpleInstance.class);
        this.sakTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("egrunnerverv-sak-instance")
                .build();
        this.journalpostTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("egrunnerverv-journalpost-instance")
                .build();
        if (retentionTimeInDays != null) {
            eventTopicService.ensureTopic(sakTopicNameParameters, Duration.ofDays(retentionTimeInDays).toMillis());
            eventTopicService.ensureTopic(journalpostTopicNameParameters, Duration.ofDays(retentionTimeInDays).toMillis());
        }
    }

    public void publishSimpleSakInstance(EgrunnervervSimpleInstance egrunnervervSimpleInstance) {
        simpleSakInstanceEventProducer.send(
                EventProducerRecord.<EgrunnervervSimpleInstance>builder()
                        .topicNameParameters(sakTopicNameParameters)
                        .value(egrunnervervSimpleInstance)
                        .build()
        );
    }

    public void publishSimpleJournalpostInstance(EgrunnervervSimpleInstance egrunnervervSimpleInstance) {
        simpleSakInstanceEventProducer.send(
                EventProducerRecord.<EgrunnervervSimpleInstance>builder()
                        .topicNameParameters(journalpostTopicNameParameters)
                        .value(egrunnervervSimpleInstance)
                        .build()
        );
    }

}

