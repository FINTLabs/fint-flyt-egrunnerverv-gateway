package no.novari.flyt.egrunnerverv.gateway.dispatch;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.egrunnerverv.gateway.dispatch.converting.InstanceFlowHeadersToInstanceHeadersEntityConvertingService;
import no.novari.flyt.egrunnerverv.gateway.dispatch.converting.InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity;
import no.novari.flyt.egrunnerverv.gateway.dispatch.repository.InstanceHeadersRepository;
import no.novari.flyt.egrunnerverv.gateway.dispatch.repository.InstanceReceiptDispatchRepository;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DispatchService {

    public static final int EGRUNNERVERV_SOURCE_APPLICATION_ID = 2;
    private final WebClientRequestService webClientRequestService;
    private final InstanceFlowHeadersToInstanceHeadersEntityConvertingService
            instanceFlowHeadersToInstanceHeadersEntityConvertingService;
    private final InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
            instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService;
    private final InstanceHeadersRepository instanceHeadersRepository;
    private final InstanceReceiptDispatchRepository instanceReceiptDispatchRepository;
    private final Sinks.Many<InstanceHeadersEntity> newInstanceHeadersEntitySink;

    public DispatchService(
            WebClientRequestService webClientRequestService,
            InstanceFlowHeadersToInstanceHeadersEntityConvertingService instanceFlowHeadersToInstanceHeadersEntityConvertingService,
            InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService,
            InstanceHeadersRepository instanceHeadersRepository,
            InstanceReceiptDispatchRepository instanceReceiptDispatchRepository
    ) {
        this.webClientRequestService = webClientRequestService;
        this.instanceFlowHeadersToInstanceHeadersEntityConvertingService =
                instanceFlowHeadersToInstanceHeadersEntityConvertingService;
        this.instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService =
                instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService;
        this.instanceHeadersRepository = instanceHeadersRepository;
        this.instanceReceiptDispatchRepository = instanceReceiptDispatchRepository;
        newInstanceHeadersEntitySink = Sinks.many().unicast().onBackpressureBuffer();
        newInstanceHeadersEntitySink.asFlux().subscribe(this::dispatch);
    }


    public void handleNewInstance(InstanceFlowHeaders instanceFlowHeaders) {
        if (instanceFlowHeaders.getSourceApplicationId() != EGRUNNERVERV_SOURCE_APPLICATION_ID) {
            return;
        }

        InstanceHeadersEntity instanceHeadersEntity = saveInstanceHeaders(instanceFlowHeaders);
        newInstanceHeadersEntitySink.tryEmitNext(instanceHeadersEntity);
    }

    public synchronized void dispatch(InstanceHeadersEntity instanceHeadersEntity) {
        convertAndTransferToInstanceReceiptDispatch(instanceHeadersEntity)
                .ifPresent(this::dispatchInstanceReceipt);
    }

    @Scheduled(
            initialDelayString = "${fint.flyt.egrunnerverv.dispatch.instance-initial-delay}",
            fixedDelayString = "${fint.flyt.egrunnerverv.dispatch.instance-fixed-delay}"
    )
    private synchronized void dispatchAll() {
        List<InstanceHeadersEntity> instanceHeaders = instanceHeadersRepository.findAll();
        if (!instanceHeaders.isEmpty()) {
            log.info("Converting and transferring {} instance header entities to instance receipt dispatch entities", instanceHeaders.size());
            instanceHeaders.forEach(
                    this::convertAndTransferToInstanceReceiptDispatch
            );
        }

        List<InstanceReceiptDispatchEntity> instanceReceipts = instanceReceiptDispatchRepository.findAll();
        if (!instanceReceipts.isEmpty()) {
            log.info("Dispatching {} instance receipt dispatch entities", instanceReceipts.size());
            instanceReceipts.forEach(
                    this::dispatchInstanceReceipt
            );
        }
    }

    private InstanceHeadersEntity saveInstanceHeaders(InstanceFlowHeaders instanceFlowHeaders) {
        InstanceHeadersEntity instanceHeadersEntity = instanceFlowHeadersToInstanceHeadersEntityConvertingService
                .convert(instanceFlowHeaders);
        log.info(
                "Saving InstanceHeadersEntity for sourceApplicationInstanceId={}",
                instanceFlowHeaders.getSourceApplicationInstanceId()
        );
        instanceHeadersRepository.save(instanceHeadersEntity);
        return instanceHeadersEntity;
    }

    private Optional<InstanceReceiptDispatchEntity> convertAndTransferToInstanceReceiptDispatch(
            InstanceHeadersEntity instanceHeadersEntity
    ) {

        String sourceApplicationInstanceId = instanceHeadersEntity.getSourceApplicationInstanceId();

        log.info(
                "Converting InstanceHeadersEntity to InstanceReceiptDispatchEntity for sourceApplicationInstanceId={}",
                sourceApplicationInstanceId
        );

        try {
            Optional<InstanceReceiptDispatchEntity> instanceReceiptDispatchEntity =
                    instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService.convert(instanceHeadersEntity)
                            .map(entity -> {
                                log.info(
                                        "Saving InstanceReceiptDispatchEntity for sourceApplicationInstanceId={}",
                                        sourceApplicationInstanceId
                                );
                                return instanceReceiptDispatchRepository.save(entity);
                            });

            if (instanceReceiptDispatchEntity.isPresent()) {
                log.info(
                        "Deleting InstanceHeadersEntity for sourceApplicationInstanceId={}",
                        sourceApplicationInstanceId
                );
                instanceHeadersRepository.delete(instanceHeadersEntity);
                log.info(
                        "Successfully converted and transferred InstanceHeadersEntity to" +
                                " InstanceReceiptDispatchEntity for sourceApplicationInstanceId={}",
                        sourceApplicationInstanceId
                );
            }

            return instanceReceiptDispatchEntity;
        } catch (RuntimeException e) {
            log.error("Converting and transferring of InstanceHeadersEntity to InstanceReceiptDispatchEntity failed" +
                    " for sourceApplicationInstanceId={}", sourceApplicationInstanceId, e);
        }

        return Optional.empty();
    }

    private void dispatchInstanceReceipt(InstanceReceiptDispatchEntity entity) {
        final String id = entity.getSourceApplicationInstanceId();

        webClientRequestService
                .dispatchInstance(entity)
                .flatMap(ok ->
                        Mono.fromRunnable(() -> instanceReceiptDispatchRepository.delete(ok))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .onErrorResume(WebClientResponseException.class, wre -> {
                    HttpStatusCode statusCode = wre.getStatusCode();
                    boolean retryable = statusCode.equals(HttpStatus.TOO_MANY_REQUESTS) || statusCode.is5xxServerError();
                    boolean terminal = !retryable;
                    if (terminal) {
                        return Mono.fromRunnable(() -> {
                                    log.warn("Terminal {} for {}, deleting row.", statusCode, id);
                                    instanceReceiptDispatchRepository.deleteById(id);
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                                .then(Mono.empty());
                    }
                    log.warn("Transient {} for {}, will retry later.", statusCode, id);
                    return Mono.error(wre);
                })
                .onErrorComplete()
                .block();
    }
}
