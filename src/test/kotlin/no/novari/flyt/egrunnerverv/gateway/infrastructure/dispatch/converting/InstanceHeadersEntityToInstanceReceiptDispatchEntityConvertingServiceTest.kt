package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.kafka.CaseRequestService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.JournalpostReceipt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Date

class InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingServiceTest {
    private val caseRequestService: CaseRequestService = mock()
    private val journalpostMapper: JournalpostToInstanceReceiptDispatchEntityConvertingService = mock()
    private val objectMapper = jacksonObjectMapper()

    private val service =
        InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService(
            tablenameSak = "sak-table",
            tablenameJournalpost = "journalpost-table",
            caseRequestService = caseRequestService,
            objectMapper = objectMapper,
            journalpostToInstanceReceiptDispatchEntityConvertingService = journalpostMapper,
        )

    @Test
    fun `convert returns null when case is missing (sak)`() {
        whenever(caseRequestService.getByMappeId("ARK-1")).thenReturn(null)

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "sak",
                    archiveInstanceId = "ARK-1",
                ),
            )

        assertThat(result).isNull()
    }

    @Test
    fun `convert builds receipt and uri for sak`() {
        val sakResource: SakResource = mock()
        whenever(sakResource.opprettetDato).thenReturn(Date.from(Instant.ofEpochSecond(0)))
        whenever(caseRequestService.getByMappeId("ARK-1")).thenReturn(sakResource)

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "sak",
                    archiveInstanceId = "ARK-1",
                ),
            )

        requireNotNull(result)
        assertThat(result.sourceApplicationInstanceId).isEqualTo("id-1")
        assertThat(result.uri)
            .isEqualTo(
                "/sak-table/id-1?sysparm_fields=arkivnummer&sysparm_query_no_domain=true",
            )

        val json = objectMapper.readTree(result.instanceReceipt)
        assertThat(json["arkivnummer"].asText()).isEqualTo("ARK-1")
        // Format is tested using same environment timezone as prod code uses
        assertThat(json["opprettelse_i_elements_fullfort"].asText())
            .matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}")
    }

    @Test
    fun `convert returns null when case is missing (journalpost)`() {
        whenever(caseRequestService.getByMappeId("CASE")).thenReturn(null)

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "journalpost",
                    archiveInstanceId = "CASE-[123]",
                ),
            )

        assertThat(result).isNull()
    }

    @Test
    fun `convert builds receipt and uri for journalpost`() {
        val sakResource: SakResource = mock()
        whenever(caseRequestService.getByMappeId("CASE")).thenReturn(sakResource)

        val receipt =
            JournalpostReceipt(
                journalpostnr = "CASE-123",
                tittel = "Tittel",
                statusId = "S",
                tilgangskode = null,
                hjemmel = null,
                dokumentdato = "01-01-2024 10:11:12",
                dokumenttypeid = "T",
                dokumenttypenavn = null,
                saksansvarligbrukernavn = null,
                saksansvarlignavn = null,
                adminenhetkortnavn = null,
                adminenhetnavn = null,
            )
        whenever(journalpostMapper.map(sakResource, 123L)).thenReturn(receipt)

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "journalpost",
                    archiveInstanceId = "CASE-[123]",
                ),
            )

        requireNotNull(result)
        assertThat(result.uri)
            .isEqualTo(
                "/journalpost-table/id-1?sysparm_fields=journalpostnr&sysparm_query_no_domain=true",
            )
        assertThat(objectMapper.readTree(result.instanceReceipt)["journalpostnr"].asText()).isEqualTo("CASE-123")
    }

    @Test
    fun `convert throws on unexpected integration id`() {
        assertThatThrownBy {
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "unknown",
                    archiveInstanceId = "X",
                ),
            )
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Unexpected value")
    }
}
