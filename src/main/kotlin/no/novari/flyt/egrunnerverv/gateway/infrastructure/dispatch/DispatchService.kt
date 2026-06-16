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
        log.atDebug {
            message = "Received instance flow headers"
            arguments =
                arrayOf(
                    kv("sourceApplicationId", instanceFlowHeaders.sourceApplicationId),
                    kv("sourceApplicationInstanceId", instanceFlowHeaders.sourceApplicationInstanceId),
                    kv("sourceApplicationIntegrationId", instanceFlowHeaders.sourceApplicationIntegrationId),
                    kv("archiveInstanceId", instanceFlowHeaders.archiveInstanceId),
                )
        }

        if (instanceFlowHeaders.sourceApplicationId != EGRUNNERVERV_SOURCE_APPLICATION_ID) {
            log.atDebug {
                message = "Skipping instance flow headers from another source application"
                arguments =
                    arrayOf(
                        kv("sourceApplicationId", instanceFlowHeaders.sourceApplicationId),
                        kv("expectedSourceApplicationId", EGRUNNERVERV_SOURCE_APPLICATION_ID),
                    )
            }
            return
        }

        val instanceHeadersEntity = saveInstanceHeaders(instanceFlowHeaders)
        log.atDebug {
            message = "Submitting instance for asynchronous dispatch"
            arguments = instanceHeadersArguments(instanceHeadersEntity)
        }
        dispatchExecutor.submit { dispatch(instanceHeadersEntity) }
    }

    @Synchronized
    fun dispatch(instanceHeadersEntity: InstanceHeadersEntity) {
        log.atDebug {
            message = "Starting dispatch of InstanceHeadersEntity"
            arguments = instanceHeadersArguments(instanceHeadersEntity)
        }

        val instanceReceiptDispatchEntity = convertAndTransferToInstanceReceiptDispatch(instanceHeadersEntity)
        if (instanceReceiptDispatchEntity == null) {
            log.atDebug {
                message = "No InstanceReceiptDispatchEntity produced, dispatch is skipped"
                arguments = instanceHeadersArguments(instanceHeadersEntity)
            }
            return
        }

        dispatchInstanceReceipt(instanceReceiptDispatchEntity)

        log.atDebug {
            message = "Finished dispatch attempt for InstanceHeadersEntity"
            arguments = instanceReceiptDispatchArguments(instanceReceiptDispatchEntity)
        }
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
        } else {
            log.atDebug {
                message = "No InstanceHeadersEntity rows found for scheduled dispatch"
            }
        }

        val instanceReceipts = instanceReceiptDispatchRepository.findAll()
        if (instanceReceipts.isNotEmpty()) {
            log.atInfo {
                message = "Dispatching instance receipt dispatch entities"
                arguments = arrayOf(kv("size", instanceReceipts.size))
            }
            instanceReceipts.forEach(::dispatchInstanceReceipt)
        } else {
            log.atDebug {
                message = "No InstanceReceiptDispatchEntity rows found for scheduled dispatch"
            }
        }
    }

    @PreDestroy
    fun shutdownExecutor() {
        log.atDebug {
            message = "Shutting down dispatch executor"
        }
        dispatchExecutor.shutdown()
    }

    private fun saveInstanceHeaders(instanceFlowHeaders: InstanceFlowHeaders): InstanceHeadersEntity {
        log.atDebug {
            message = "Converting InstanceFlowHeaders to InstanceHeadersEntity"
            arguments =
                arrayOf(
                    kv("sourceApplicationId", instanceFlowHeaders.sourceApplicationId),
                    kv("sourceApplicationInstanceId", instanceFlowHeaders.sourceApplicationInstanceId),
                    kv("sourceApplicationIntegrationId", instanceFlowHeaders.sourceApplicationIntegrationId),
                    kv("archiveInstanceId", instanceFlowHeaders.archiveInstanceId),
                )
        }

        val instanceHeadersEntity =
            instanceFlowHeadersToInstanceHeadersEntityConvertingService.convert(
                instanceFlowHeaders,
            )
        log.atInfo {
            message = "Saving InstanceHeadersEntity"
            arguments = arrayOf(kv("sourceApplicationInstanceId", instanceFlowHeaders.sourceApplicationInstanceId))
        }
        val savedInstanceHeadersEntity = instanceHeadersRepository.save(instanceHeadersEntity)

        log.atDebug {
            message = "Saved InstanceHeadersEntity"
            arguments = instanceHeadersArguments(savedInstanceHeadersEntity)
        }

        return savedInstanceHeadersEntity
    }

    private fun convertAndTransferToInstanceReceiptDispatch(
        instanceHeadersEntity: InstanceHeadersEntity,
    ): InstanceReceiptDispatchEntity? {
        val sourceApplicationInstanceId = instanceHeadersEntity.sourceApplicationInstanceId

        log.atInfo {
            message = "Converting InstanceHeadersEntity to InstanceReceiptDispatchEntity"
            arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
        }
        log.atDebug {
            message = "Converting InstanceHeadersEntity to InstanceReceiptDispatchEntity"
            arguments = instanceHeadersArguments(instanceHeadersEntity)
        }

        return try {
            val instanceReceiptDispatchEntity =
                instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
                    .convert(
                        instanceHeadersEntity,
                    )?.let { entity ->
                        log.atDebug {
                            message = "Converted InstanceHeadersEntity to InstanceReceiptDispatchEntity"
                            arguments = instanceReceiptDispatchArguments(entity)
                        }
                        log.atInfo {
                            message = "Saving InstanceReceiptDispatchEntity"
                            arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
                        }
                        val savedEntity = instanceReceiptDispatchRepository.save(entity)

                        log.atDebug {
                            message = "Saved InstanceReceiptDispatchEntity"
                            arguments = instanceReceiptDispatchArguments(savedEntity)
                        }

                        savedEntity
                    }

            if (instanceReceiptDispatchEntity != null) {
                log.atInfo {
                    message = "Deleting InstanceHeadersEntity"
                    arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
                }
                instanceHeadersRepository.delete(instanceHeadersEntity)
                log.atDebug {
                    message = "Deleted InstanceHeadersEntity after transfer to dispatch queue"
                    arguments = instanceHeadersArguments(instanceHeadersEntity)
                }
                log.atInfo {
                    message =
                        "Successfully converted and transferred InstanceHeadersEntity to InstanceReceiptDispatchEntity"
                    arguments = arrayOf(kv("sourceApplicationInstanceId", sourceApplicationInstanceId))
                }
            } else {
                log.atDebug {
                    message = "InstanceHeadersEntity conversion returned no dispatch entity"
                    arguments = instanceHeadersArguments(instanceHeadersEntity)
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
            log.atDebug {
                message = "Dispatching InstanceReceiptDispatchEntity to ServiceNow"
                arguments = instanceReceiptDispatchArguments(entity)
            }
            restClientRequestService.dispatchInstance(entity)
            log.atDebug {
                message = "ServiceNow dispatch completed, deleting InstanceReceiptDispatchEntity"
                arguments = instanceReceiptDispatchArguments(entity)
            }
            instanceReceiptDispatchRepository.delete(entity)
            log.atDebug {
                message = "Deleted InstanceReceiptDispatchEntity after successful ServiceNow dispatch"
                arguments = instanceReceiptDispatchArguments(entity)
            }
        } catch (exception: RestClientResponseException) {
            val statusCode = exception.statusCode
            val retryable = statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode.is5xxServerError
            log.atDebug {
                cause = exception
                message = "ServiceNow dispatch failed with response"
                arguments =
                    arrayOf(
                        kv("statusCode", statusCode.value()),
                        kv("id", id),
                        kv("uri", entity.uri),
                        kv("responseBody", exception.responseBodyAsString),
                    )
            }
            if (!retryable) {
                log.atWarn {
                    cause = exception
                    message = "Terminal failure, deleting row."
                    arguments = arrayOf(kv("statusCode", statusCode.value()), kv("id", id), kv("uri", entity.uri))
                }
                instanceReceiptDispatchRepository.deleteById(id)
                log.atDebug {
                    message = "Deleted terminally failed InstanceReceiptDispatchEntity"
                    arguments =
                        arrayOf(
                            kv("statusCode", statusCode.value()),
                            kv("id", id),
                            kv("uri", entity.uri),
                        )
                }
            } else {
                log.atWarn {
                    cause = exception
                    message = "Transient failure, will retry later."
                    arguments = arrayOf(kv("statusCode", statusCode.value()), kv("id", id), kv("uri", entity.uri))
                }
            }
        } catch (exception: Exception) {
            log.atDebug {
                cause = exception
                message = "Unexpected failure during ServiceNow dispatch"
                arguments = instanceReceiptDispatchArguments(entity)
            }
            log.atWarn {
                cause = exception
                message = "Dispatch failed, will retry later."
                arguments = arrayOf(kv("id", id), kv("uri", entity.uri))
            }
        }
    }

    private fun instanceHeadersArguments(entity: InstanceHeadersEntity): Array<Any?> =
        arrayOf<Any?>(
            kv("sourceApplicationInstanceId", entity.sourceApplicationInstanceId),
            kv("sourceApplicationIntegrationId", entity.sourceApplicationIntegrationId),
            kv("archiveInstanceId", entity.archiveInstanceId),
        )

    private fun instanceReceiptDispatchArguments(entity: InstanceReceiptDispatchEntity): Array<Any?> =
        arrayOf<Any?>(
            kv("sourceApplicationInstanceId", entity.sourceApplicationInstanceId),
            kv("uri", entity.uri),
            kv("instanceReceipt", entity.instanceReceipt),
        )

    private companion object {
        private const val EGRUNNERVERV_SOURCE_APPLICATION_ID = 2L
        private val log = KotlinLogging.logger {}
    }
}
