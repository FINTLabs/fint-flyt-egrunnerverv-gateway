package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InstanceFlowHeadersToInstanceHeadersEntityConvertingServiceTest {
    private val service = InstanceFlowHeadersToInstanceHeadersEntityConvertingService()

    @Test
    fun `convert maps ids and defaults archiveInstanceId to empty string`() {
        val headers: InstanceFlowHeaders = mock()
        whenever(headers.sourceApplicationInstanceId).thenReturn("src-id")
        whenever(headers.sourceApplicationIntegrationId).thenReturn("sak")
        whenever(headers.archiveInstanceId).thenReturn(null)

        val result = service.convert(headers)

        assertThat(result.sourceApplicationInstanceId).isEqualTo("src-id")
        assertThat(result.sourceApplicationIntegrationId).isEqualTo("sak")
        assertThat(result.archiveInstanceId).isEqualTo("")
    }

    @Test
    fun `convert throws when sourceApplicationInstanceId is missing`() {
        val headers: InstanceFlowHeaders = mock()
        whenever(headers.sourceApplicationInstanceId).thenReturn(null)
        whenever(headers.sourceApplicationIntegrationId).thenReturn("sak")

        assertThatThrownBy { service.convert(headers) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Missing sourceApplicationInstanceId")
    }

    @Test
    fun `convert throws when sourceApplicationIntegrationId is missing`() {
        val headers: InstanceFlowHeaders = mock()
        whenever(headers.sourceApplicationInstanceId).thenReturn("src-id")
        whenever(headers.sourceApplicationIntegrationId).thenReturn(null)

        assertThatThrownBy { service.convert(headers) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Missing sourceApplicationIntegrationId")
    }
}
