package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.RestClientRequestService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.archive.ArchiveGatewayClient
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

class InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingServiceTest {
    private val baseUrl = "http://gateway.test"
    private val restClientBuilder = RestClient.builder().baseUrl(baseUrl)
    private val server = MockRestServiceServer.bindTo(restClientBuilder).build()
    private val restClient = restClientBuilder.build()
    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }
    private val restClientRequestService = RestClientRequestService(restClient, jsonMapper)

    private val service =
        InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService(
            tablenameSak = "sak-table",
            tablenameJournalpost = "journalpost-table",
            archiveGatewayClient = ArchiveGatewayClient(restClient),
            jsonMapper = jsonMapper,
            journalpostToInstanceReceiptDispatchEntityConvertingService =
                JournalpostToInstanceReceiptDispatchEntityConvertingService(
                    administrativEnhetResourceCache = mock(),
                    arkivressursResourceCache = mock(),
                    journalStatusResourceCache = mock(),
                    journalpostTypeResourceCache = mock(),
                    tilgangsrestriksjonResourceCache = mock(),
                    skjermingshjemmelResourceCache = mock(),
                    personalressursResourceCache = mock(),
                    personResourceCache = mock(),
                ),
        )

    @AfterEach
    fun verify() {
        server.verify()
    }

    @Test
    fun `convert returns null when case is missing (sak)`() {
        server
            .expect(requestTo("$baseUrl/arkiv/saker?archiveInstanceId=20249999"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "sak",
                    archiveInstanceId = "20249999",
                ),
            )

        assertThat(result).isNull()
    }

    @Test
    fun `looks up a compact case id and sends it in the sak receipt`() {
        server
            .expect(requestTo("$baseUrl/arkiv/saker?archiveInstanceId=2024123456"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    """{"mappeId":{"identifikatorverdi":"2024123456"},"opprettetDato":0}""",
                    MediaType.APPLICATION_JSON,
                ),
            )
        server
            .expect(requestTo("$baseUrl/sak-table/id-1?sysparm_fields=arkivnummer&sysparm_query_no_domain=true"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().json("""{"arkivnummer":"2024123456"}"""))
            .andRespond(withNoContent())

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "sak",
                    archiveInstanceId = "2024123456",
                ),
            )

        requireNotNull(result)
        assertThat(result.sourceApplicationInstanceId).isEqualTo("id-1")

        val json = jsonMapper.readTree(result.instanceReceipt)
        assertThat(json["opprettelse_i_elements_fullfort"].asString())
            .matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}")
        restClientRequestService.dispatchInstance(result)
    }

    @Test
    fun `convert returns null when case is missing (journalpost)`() {
        server
            .expect(requestTo("$baseUrl/arkiv/saker?archiveInstanceId=202412345-%5B8%5D"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "journalpost",
                    archiveInstanceId = "202412345-[8]",
                ),
            )

        assertThat(result).isNull()
    }

    @Test
    fun `looks up the full journalpost id and sends the filtered journalpost in the receipt`() {
        val receiptQuery = "sysparm_fields=journalpostnr&sysparm_query_no_domain=true"
        server
            .expect(requestTo("$baseUrl/arkiv/saker?archiveInstanceId=202412345-%5B8%5D"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    """
                    {
                      "mappeId": {"identifikatorverdi":"202412345"},
                      "journalpost": [{"journalPostnummer":8,"tittel":"Tittel"}]
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )
        server
            .expect(requestTo("$baseUrl/journalpost-table/id-1?$receiptQuery"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().json("""{"journalpostnr":"202412345-8","tittel":"Tittel"}"""))
            .andRespond(withNoContent())

        val result =
            service.convert(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "journalpost",
                    archiveInstanceId = "202412345-[8]",
                ),
            )

        restClientRequestService.dispatchInstance(requireNotNull(result))
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
