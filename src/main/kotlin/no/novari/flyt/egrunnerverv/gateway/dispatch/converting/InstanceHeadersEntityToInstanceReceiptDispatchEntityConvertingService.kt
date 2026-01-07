package no.novari.flyt.egrunnerverv.gateway.dispatch.converting

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.egrunnerverv.gateway.dispatch.kafka.CaseRequestService
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.JournalpostReceipt
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.SakReceipt
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
            else -> throw IllegalStateException(
                "Unexpected value: ${instanceHeadersEntity.sourceApplicationIntegrationId}",
            )
        }

    private fun convertSak(instanceHeadersEntity: InstanceHeadersEntity): InstanceReceiptDispatchEntity? {
        val archiveInstanceId = instanceHeadersEntity.archiveInstanceId
        val sourceApplicationInstanceId = instanceHeadersEntity.sourceApplicationInstanceId

        val sakResource = caseRequestService.getByMappeId(archiveInstanceId) ?: return null
        val sakReceipt =
            SakReceipt(
                arkivnummer = archiveInstanceId,
                opprettelse_i_elements_fullfort = formatOrNull(sakResource.opprettetDato),
            )

        val uri = buildUri(tablenameSak, sourceApplicationInstanceId, "arkivnummer")

        return InstanceReceiptDispatchEntity(
            sourceApplicationInstanceId = sourceApplicationInstanceId,
            instanceReceipt = toJson(sakReceipt),
            classType = SakReceipt::class.java,
            uri = uri,
        )
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

        val sakResource = caseRequestService.getByMappeId(caseId) ?: return null
        val journalpostReceipt =
            journalpostToInstanceReceiptDispatchEntityConvertingService
                .map(sakResource, journalpostNummer)

        val uri = buildUri(tablenameJournalpost, sourceApplicationInstanceId, "journalpostnr")

        return InstanceReceiptDispatchEntity(
            sourceApplicationInstanceId = sourceApplicationInstanceId,
            instanceReceipt = toJson(journalpostReceipt),
            classType = JournalpostReceipt::class.java,
            uri = uri,
        )
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
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Failed to serialize instance receipt: $value", e)
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
