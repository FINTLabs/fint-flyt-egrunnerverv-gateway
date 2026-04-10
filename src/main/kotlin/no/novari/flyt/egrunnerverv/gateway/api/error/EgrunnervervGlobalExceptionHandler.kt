package no.novari.flyt.egrunnerverv.gateway.api.error

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import net.logstash.logback.argument.StructuredArguments.kv
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.OffsetDateTime

@RestControllerAdvice
class EgrunnervervGlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        log.atWarn {
            cause = exception
            message = "Invalid request"
            arguments = arrayOf(kv("path", request.requestURI))
        }

        return buildError(HttpStatus.BAD_REQUEST, "Invalid request", request.requestURI)
    }

    private fun buildError(
        status: HttpStatus,
        message: String?,
        path: String,
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    timestamp = OffsetDateTime.now(),
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    path = path,
                ),
            )
    }

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}
