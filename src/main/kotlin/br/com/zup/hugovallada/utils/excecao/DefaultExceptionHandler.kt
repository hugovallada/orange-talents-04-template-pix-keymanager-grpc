package br.com.zup.hugovallada.utils.excecao

import br.com.zup.hugovallada.utils.excecao.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is ExistingPixKeyException -> Status.ALREADY_EXISTS.withDescription(e.message)
            is ClientNotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            is PixKeyNotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            is PermissionDeniedException -> Status.PERMISSION_DENIED.withDescription(e.message)
            is HttpClientResponseException -> Status.ABORTED.withDescription("Um erro aconteceu no serviço externo")
            else -> Status.UNKNOWN.withDescription("Um erro aconteceu")
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}
