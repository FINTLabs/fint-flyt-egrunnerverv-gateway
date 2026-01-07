package no.novari.flyt.egrunnerverv.gateway.dispatch

import no.novari.flyt.egrunnerverv.gateway.dispatch.converting.InstanceFlowHeadersToInstanceHeadersEntityConvertingService
import no.novari.flyt.egrunnerverv.gateway.dispatch.converting.InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity
import no.novari.flyt.egrunnerverv.gateway.dispatch.repository.InstanceHeadersRepository
import no.novari.flyt.egrunnerverv.gateway.dispatch.repository.InstanceReceiptDispatchRepository
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class DispatchService(
    private val webClientRequestService: WebClientRequestService,
    private val instanceFlowHeadersToInstanceHeadersEntityConvertingService:
        InstanceFlowHeadersToInstanceHeadersEntityConvertingService,
    private val instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService:
        InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService,
    private val instanceHeadersRepository: InstanceHeadersRepository,
    private val instanceReceiptDispatchRepository: InstanceReceiptDispatchRepository,
) {
    fun handleNewInstance(instanceFlowHeaders: InstanceFlowHeaders) {
        if (instanceFlowHeaders.sourceApplicationId != EGRUNNERVERV_SOURCE_APPLICATION_ID) {
            return
        }

        val instanceHeadersEntity = saveInstanceHeaders(instanceFlowHeaders)
        dispatch(instanceHeadersEntity)
    }

    @Synchronized
    fun dispatch(instanceHeadersEntity: InstanceHeadersEntity) {
        convertAndTransferToInstanceReceiptDispatch(instanceHeadersEntity)
            ?.let(this::dispatchInstanceReceipt)
    }

    @Scheduled(
        initialDelayString = "\${novari.flyt.egrunnerverv.dispatch.instance-initial-delay}",
        fixedDelayString = "\${novari.flyt.egrunnerverv.dispatch.instance-fixed-delay}",
    )
    @Synchronized
    private fun dispatchAll() {
        val instanceHeaders = instanceHeadersRepository.findAll()
        if (instanceHeaders.isNotEmpty()) {
            logger.info(
                "Converting and transferring {} instance header entities to instance receipt dispatch entities",
                instanceHeaders.size,
            )
            instanceHeaders.forEach(this::convertAndTransferToInstanceReceiptDispatch)
        }

        val instanceReceipts = instanceReceiptDispatchRepository.findAll()
        if (instanceReceipts.isNotEmpty()) {
            logger.info("Dispatching {} instance receipt dispatch entities", instanceReceipts.size)
            instanceReceipts.forEach(this::dispatchInstanceReceipt)
        }
    }

    private fun saveInstanceHeaders(instanceFlowHeaders: InstanceFlowHeaders): InstanceHeadersEntity {
        val instanceHeadersEntity =
            instanceFlowHeadersToInstanceHeadersEntityConvertingService
                .convert(instanceFlowHeaders)
        logger.info(
            "Saving InstanceHeadersEntity for sourceApplicationInstanceId={}",
            instanceFlowHeaders.sourceApplicationInstanceId,
        )
        instanceHeadersRepository.save(instanceHeadersEntity)
        return instanceHeadersEntity
    }

    private fun convertAndTransferToInstanceReceiptDispatch(
        instanceHeadersEntity: InstanceHeadersEntity,
    ): InstanceReceiptDispatchEntity? {
        val sourceApplicationInstanceId = instanceHeadersEntity.sourceApplicationInstanceId

        logger.info(
            "Converting InstanceHeadersEntity to InstanceReceiptDispatchEntity for sourceApplicationInstanceId={}",
            sourceApplicationInstanceId,
        )

        return try {
            val instanceReceiptDispatchEntity =
                instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
                    .convert(instanceHeadersEntity)
                    ?.let { entity ->
                        logger.info(
                            "Saving InstanceReceiptDispatchEntity for sourceApplicationInstanceId={}",
                            sourceApplicationInstanceId,
                        )
                        instanceReceiptDispatchRepository.save(entity)
                    }

            if (instanceReceiptDispatchEntity != null) {
                logger.info(
                    "Deleting InstanceHeadersEntity for sourceApplicationInstanceId={}",
                    sourceApplicationInstanceId,
                )
                instanceHeadersRepository.delete(instanceHeadersEntity)
                logger.info(
                    "Successfully converted and transferred InstanceHeadersEntity to " +
                        "InstanceReceiptDispatchEntity for sourceApplicationInstanceId={}",
                    sourceApplicationInstanceId,
                )
            }

            instanceReceiptDispatchEntity
        } catch (e: RuntimeException) {
            logger.error(
                "Converting and transferring of InstanceHeadersEntity to InstanceReceiptDispatchEntity failed " +
                    "for sourceApplicationInstanceId={}",
                sourceApplicationInstanceId,
                e,
            )
            null
        }
    }

    private fun dispatchInstanceReceipt(entity: InstanceReceiptDispatchEntity) {
        val id = entity.sourceApplicationInstanceId

        try {
            webClientRequestService.dispatchInstance(entity)
            instanceReceiptDispatchRepository.delete(entity)
        } catch (ex: RestClientResponseException) {
            val statusCode: HttpStatusCode = ex.statusCode
            val retryable =
                statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode.is5xxServerError
            val terminal = !retryable
            if (terminal) {
                logger.warn("Terminal {} for {}, deleting row.", statusCode, id)
                instanceReceiptDispatchRepository.deleteById(id)
                return
            }
            logger.warn("Transient {} for {}, will retry later.", statusCode, id)
        }
    }

    companion object {
        private const val EGRUNNERVERV_SOURCE_APPLICATION_ID: Long = 2L
        private val logger = LoggerFactory.getLogger(DispatchService::class.java)
    }
}
