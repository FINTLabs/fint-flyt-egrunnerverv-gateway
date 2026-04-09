package no.novari.flyt.egrunnerverv.gateway.api.error

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import net.logstash.logback.argument.StructuredArguments.kv
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestControllerAdvice
class EgrunnervervGlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        exception: ResponseStatusException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val status = HttpStatus.valueOf(exception.statusCode.value())
        logStatusException(status, request.requestURI, exception)
        val detail =
            if (status.is5xxServerError) {
                "Internal server error"
            } else {
                exception.reason ?: status.reasonPhrase
            }
        return buildResponse(status, detail, request)
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        MethodArgumentNotValidException::class,
        ConstraintViolationException::class,
        HttpMessageNotReadableException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
    )
    fun handleBadRequest(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        log.atWarn {
            cause = exception
            message = "Invalid request"
            arguments = arrayOf(kv("path", request.requestURI))
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        log.atError {
            cause = exception
            message = "Unexpected error"
            arguments = arrayOf(kv("requestUri", request.requestURI))
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request)
    }

    private fun logStatusException(
        status: HttpStatus,
        path: String,
        exception: ResponseStatusException,
    ) {
        if (status.is5xxServerError) {
            log.atError {
                cause = exception
                message = "Request failed"
                arguments = arrayOf(kv("status", status.value()), kv("path", path))
            }
        } else {
            log.atWarn {
                cause = exception
                message = "Request failed"
                arguments = arrayOf(kv("status", status.value()), kv("path", path))
            }
        }
    }

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, message)
        problemDetail.title = status.reasonPhrase
        problemDetail.setProperty("path", request.requestURI)
        problemDetail.setProperty("timestamp", Instant.now().toString())
        return ResponseEntity.status(status).body(problemDetail)
    }

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}
