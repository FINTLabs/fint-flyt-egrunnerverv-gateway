package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting.InstanceFlowHeadersToInstanceHeadersEntityConvertingService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting.InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceReceiptDispatchEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.repository.InstanceHeadersRepository
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.repository.InstanceReceiptDispatchRepository
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class DispatchService(
    private val restClientRequestService: RestClientRequestService,
    private val instanceFlowHeadersToInstanceHeadersEntityConvertingService:
        InstanceFlowHeadersToInstanceHeadersEntityConvertingService,
    private val instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService:
        InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService,
    private val instanceHeadersRepository: InstanceHeadersRepository,
    private val instanceReceiptDispatchRepository: InstanceReceiptDispatchRepository,
) {
    private val dispatchExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun handleNewInstance(instanceFlowHeaders: InstanceFlowHeaders) {
        if (instanceFlowHeaders.sourceApplicationId != EGRUNNERVERV_SOURCE_APPLICATION_ID) {
            return
        }

        val instanceHeadersEntity = saveInstanceHeaders(instanceFlowHeaders)
        dispatchExecutor.submit { dispatch(instanceHeadersEntity) }
    }

    @Synchronized
    fun dispatch(instanceHeadersEntity: InstanceHeadersEntity) {
        convertAndTransferToInstanceReceiptDispatch(instanceHeadersEntity)?.let(::dispatchInstanceReceipt)
    }

    @Scheduled(
        initialDelayString = "\${novari.flyt.egrunnerverv.dispatch.instance-initial-delay}",
        fixedDelayString = "\${novari.flyt.egrunnerverv.dispatch.instance-fixed-delay}",
    )
    @Synchronized
    fun dispatchAll() {
        val instanceHeaders = instanceHeadersRepository.findAll()
        if (instanceHeaders.isNotEmpty()) {
            log.atInfo {
                message = "Converting and transferring instance header entities to instance receipt dispatch entities"
                arguments = arrayOf(kv("size", instanceHeaders.size))
            }
            instanceHeaders.forEach(::convertAndTransferToInstanceReceiptDispatch)
        }

        val instanceReceipts = instanceReceiptDispatchRepository.findAll()
        if (instanceReceipts.isNotEmpty()) {
            log.atInfo {
                message = "Dispatching instance receipt dispatch entities"
                arguments = arrayOf(kv("size", instanceReceipts.size))
            }
            instanceReceipts.forEach(::dispatchInstanceReceipt)
        }
    }

    @PreDestroy
    fun shutdownExecutor() {
        dispatchExecutor.shutdown()
    }

    private fun saveInstanceHeaders(instanceFlowHeaders: InstanceFlowHeaders): InstanceHeadersEntity {
        val instanceHeadersEntity =
            instanceFlowHeadersToInstanceHeadersEntityConvertingService.convert(
                instanceFlowHeaders,
            )
        log.atInfo {
            message = "Saving InstanceHeadersEntity"
            arguments = arrayOf(kv("sourceApplicationInstanceId", instanceFlowHeaders.sourceApplicationInstanceId))
        }
        return instanceHeadersRepository.save(instanceHeadersEntity)
    }

    private fun convertAndTransferToInstanceReceiptDispatch(
        instanceHeadersEntity: InstanceHeadersEntity,
    ): InstanceReceiptDispatchEntity? {
        val sourceApplicationInstanceId = instanceHeadersEntity.sourceApplicationInstanceId

        log.atInfo {
            message = "Converting InstanceHeadersEntity to InstanceReceiptDispatchEntity"
            arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
        }

        return try {
            val instanceReceiptDispatchEntity =
                instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
                    .convert(
                        instanceHeadersEntity,
                    )?.let { entity ->
                        log.atInfo {
                            message = "Saving InstanceReceiptDispatchEntity"
                            arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
                        }
                        instanceReceiptDispatchRepository.save(entity)
                    }

            if (instanceReceiptDispatchEntity != null) {
                log.atInfo {
                    message = "Deleting InstanceHeadersEntity"
                    arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
                }
                instanceHeadersRepository.delete(instanceHeadersEntity)
                log.atInfo {
                    message =
                        "Successfully converted and transferred InstanceHeadersEntity to InstanceReceiptDispatchEntity"
                    arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
                }
            }

            instanceReceiptDispatchEntity
        } catch (exception: RuntimeException) {
            log.atError {
                cause = exception
                message = "Converting and transferring of InstanceHeadersEntity to InstanceReceiptDispatchEntity failed"
                arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
            }
            null
        }
    }

    private fun dispatchInstanceReceipt(entity: InstanceReceiptDispatchEntity) {
        val id = entity.sourceApplicationInstanceId

        try {
            restClientRequestService.dispatchInstance(entity)
            instanceReceiptDispatchRepository.delete(entity)
        } catch (exception: RestClientResponseException) {
            val statusCode = exception.statusCode
            val retryable = statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode.is5xxServerError
            if (!retryable) {
                log.atWarn {
                    cause = exception
                    message = "Terminal failure, deleting row."
                    arguments = arrayOf(kv("statuCode", statusCode.value()), kv("id", id))
                }
                instanceReceiptDispatchRepository.deleteById(id)
            } else {
                log.atWarn {
                    cause = exception
                    message = "Transient failure, will retry later."
                    arguments = arrayOf(kv("statuCode", statusCode.value()), kv("id", id))
                }
            }
        } catch (exception: Exception) {
            log.atWarn {
                cause = exception
                message = "Dispatch failed, will retry later."
                arguments = arrayOf(kv("id", id))
            }
        }
    }

    private companion object {
        private const val EGRUNNERVERV_SOURCE_APPLICATION_ID = 2L
        private val log = KotlinLogging.logger {}
    }
}
