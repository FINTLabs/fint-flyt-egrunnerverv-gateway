package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch

import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting.InstanceFlowHeadersToInstanceHeadersEntityConvertingService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting.InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceReceiptDispatchEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.repository.InstanceHeadersRepository
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.repository.InstanceReceiptDispatchRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClientResponseException
import java.nio.charset.StandardCharsets

class DispatchServiceTest {
    private val restClientRequestService: RestClientRequestService = mock()
    private val headersConverter: InstanceFlowHeadersToInstanceHeadersEntityConvertingService = mock()
    private val dispatchConverter: InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService = mock()
    private val instanceHeadersRepository: InstanceHeadersRepository = mock()
    private val instanceReceiptDispatchRepository: InstanceReceiptDispatchRepository = mock()

    private val service =
        DispatchService(
            restClientRequestService = restClientRequestService,
            instanceFlowHeadersToInstanceHeadersEntityConvertingService = headersConverter,
            instanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService = dispatchConverter,
            instanceHeadersRepository = instanceHeadersRepository,
            instanceReceiptDispatchRepository = instanceReceiptDispatchRepository,
        )

    @Test
    fun `dispatch deletes both rows on success`() {
        val header = InstanceHeadersEntity(sourceApplicationInstanceId = "id-1")
        val dispatch =
            InstanceReceiptDispatchEntity(
                sourceApplicationInstanceId = "id-1",
                uri = "http://example",
                instanceReceipt = "{}",
            )

        whenever(dispatchConverter.convert(header)).thenReturn(dispatch)
        whenever(instanceReceiptDispatchRepository.save(dispatch)).thenReturn(dispatch)

        service.dispatch(header)

        verify(instanceReceiptDispatchRepository).save(dispatch)
        verify(instanceHeadersRepository).delete(header)
        verify(restClientRequestService).dispatchInstance(dispatch)
        verify(instanceReceiptDispatchRepository).delete(dispatch)
        verify(instanceReceiptDispatchRepository, never()).deleteById("id-1")
    }

    @Test
    fun `dispatch deletes receipt row by id on terminal RestClientResponseException`() {
        val header = InstanceHeadersEntity(sourceApplicationInstanceId = "id-1")
        val dispatch =
            InstanceReceiptDispatchEntity(
                sourceApplicationInstanceId = "id-1",
                uri = "http://example",
                instanceReceipt = "{}",
            )

        whenever(dispatchConverter.convert(header)).thenReturn(dispatch)
        whenever(instanceReceiptDispatchRepository.save(dispatch)).thenReturn(dispatch)
        doThrow(restClientResponseException(HttpStatus.BAD_REQUEST))
            .whenever(restClientRequestService)
            .dispatchInstance(dispatch)

        service.dispatch(header)

        verify(instanceHeadersRepository).delete(header)
        verify(instanceReceiptDispatchRepository, never()).delete(dispatch)
        verify(instanceReceiptDispatchRepository).deleteById("id-1")
    }

    @Test
    fun `dispatch keeps receipt row on transient RestClientResponseException`() {
        val header = InstanceHeadersEntity(sourceApplicationInstanceId = "id-1")
        val dispatch =
            InstanceReceiptDispatchEntity(
                sourceApplicationInstanceId = "id-1",
                uri = "http://example",
                instanceReceipt = "{}",
            )

        whenever(dispatchConverter.convert(header)).thenReturn(dispatch)
        whenever(instanceReceiptDispatchRepository.save(dispatch)).thenReturn(dispatch)
        doThrow(restClientResponseException(HttpStatus.TOO_MANY_REQUESTS))
            .whenever(restClientRequestService)
            .dispatchInstance(dispatch)

        service.dispatch(header)

        verify(instanceHeadersRepository).delete(header)
        verify(instanceReceiptDispatchRepository, never()).delete(dispatch)
        verify(instanceReceiptDispatchRepository, never()).deleteById("id-1")
    }

    @Test
    fun `dispatch keeps receipt row on unexpected exception`() {
        val header = InstanceHeadersEntity(sourceApplicationInstanceId = "id-1")
        val dispatch =
            InstanceReceiptDispatchEntity(
                sourceApplicationInstanceId = "id-1",
                uri = "http://example",
                instanceReceipt = "{}",
            )

        whenever(dispatchConverter.convert(header)).thenReturn(dispatch)
        whenever(instanceReceiptDispatchRepository.save(dispatch)).thenReturn(dispatch)
        doThrow(RuntimeException("boom"))
            .whenever(restClientRequestService)
            .dispatchInstance(dispatch)

        service.dispatch(header)

        verify(instanceHeadersRepository).delete(header)
        verify(instanceReceiptDispatchRepository, never()).delete(dispatch)
        verify(instanceReceiptDispatchRepository, never()).deleteById("id-1")
    }

    @Test
    fun `dispatch does nothing when converter returns null`() {
        val header = InstanceHeadersEntity(sourceApplicationInstanceId = "id-1")
        whenever(dispatchConverter.convert(header)).thenReturn(null)

        service.dispatch(header)

        verify(instanceReceiptDispatchRepository, never()).save(any())
        verify(restClientRequestService, never()).dispatchInstance(any())
    }

    private fun restClientResponseException(status: HttpStatus): RestClientResponseException =
        RestClientResponseException(
            "error",
            status.value(),
            status.reasonPhrase,
            HttpHeaders(),
            "body".toByteArray(),
            StandardCharsets.UTF_8,
        )
}
