package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.kafka.CaseRequestService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceReceiptDispatchEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.JournalpostReceipt
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.SakReceipt
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@Service
class InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService(
    @param:Value("\${novari.flyt.egrunnerverv.dispatch.tablenameSak}")
    private val tablenameSak: String,
    @param:Value("\${novari.flyt.egrunnerverv.dispatch.tablenameJournalpost}")
    private val tablenameJournalpost: String,
    private val caseRequestService: CaseRequestService,
    private val objectMapper: ObjectMapper,
    private val journalpostToInstanceReceiptDispatchEntityConvertingService:
        JournalpostToInstanceReceiptDispatchEntityConvertingService,
) {
    fun convert(instanceHeadersEntity: InstanceHeadersEntity): InstanceReceiptDispatchEntity? =
        when (instanceHeadersEntity.sourceApplicationIntegrationId) {
            SAK -> convertSak(instanceHeadersEntity)
            JOURNALPOST -> convertJournalpost(instanceHeadersEntity)
            else -> error("Unexpected value: ${instanceHeadersEntity.sourceApplicationIntegrationId}")
        }

    private fun convertSak(instanceHeadersEntity: InstanceHeadersEntity): InstanceReceiptDispatchEntity? {
        val archiveInstanceId = instanceHeadersEntity.archiveInstanceId
        val sourceApplicationInstanceId = instanceHeadersEntity.sourceApplicationInstanceId

        return caseRequestService.getByMappeId(archiveInstanceId)?.let { sakResource ->
            val sakReceipt =
                SakReceipt(
                    arkivnummer = archiveInstanceId,
                    opprettelseIElementsFullfort = formatOrNull(sakResource.opprettetDato),
                )

            InstanceReceiptDispatchEntity(
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                uri = buildUri(tablenameSak, sourceApplicationInstanceId, "arkivnummer"),
                instanceReceipt = toJson(sakReceipt),
            )
        }
    }

    private fun convertJournalpost(instanceHeadersEntity: InstanceHeadersEntity): InstanceReceiptDispatchEntity? {
        val sourceApplicationInstanceId = instanceHeadersEntity.sourceApplicationInstanceId
        val splitArchiveInstanceId = instanceHeadersEntity.archiveInstanceId.split("-")
        val caseId = splitArchiveInstanceId[0]
        val journalpostNummer =
            splitArchiveInstanceId[1]
                .replace("[", "")
                .replace("]", "")
                .toLong()

        return caseRequestService.getByMappeId(caseId)?.let { sakResource ->
            val journalpostReceipt =
                journalpostToInstanceReceiptDispatchEntityConvertingService.map(sakResource, journalpostNummer)

            InstanceReceiptDispatchEntity(
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                uri = buildUri(tablenameJournalpost, sourceApplicationInstanceId, "journalpostnr"),
                instanceReceipt = toJson(journalpostReceipt),
            )
        }
    }

    private fun buildUri(
        tableName: String,
        instanceId: String,
        fieldName: String,
    ): String =
        UriComponentsBuilder
            .newInstance()
            .pathSegment(tableName, instanceId)
            .queryParam("sysparm_fields", fieldName)
            .queryParam("sysparm_query_no_domain", "true")
            .toUriString()

    private fun toJson(value: Any): String =
        try {
            objectMapper.writeValueAsString(value)
        } catch (exception: JsonProcessingException) {
            throw RuntimeException("Failed to serialize instance receipt: $value", exception)
        }

    private fun formatOrNull(date: Date?): String? =
        date
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDateTime()
            ?.format(DateTimeFormatter.ofPattern(EGRUNNERVERV_DATETIME_FORMAT))

    companion object {
        const val EGRUNNERVERV_DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss"
        private const val SAK = "sak"
        private const val JOURNALPOST = "journalpost"
    }
}
