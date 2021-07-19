package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.dispatchers.GrpcExceptionDispatcher
import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.core.handler.CustomErrorHandler
import br.com.zup.academy.armando.core.handler.exceptions.CustomGrpcResponseException
import br.com.zup.academy.armando.pix.dtos.RemovePixRequestDto
import br.com.zupedu.armando.PixKeyManagerRemoverServiceGrpc
import br.com.zupedu.armando.RemoverPixRequest
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.PathVariable
import io.micronaut.validation.Validated
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Controller
@Validated
@CustomErrorHandler
class RemovePixKeyController(
    private val grpcClient: PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceBlockingStub,
    private val validator: Validator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Delete("/api/v1/pix/{pixId}")
    fun removePix(
        @PathVariable pixId: String,
        clientId: String
    ): HttpResponse<Any> {
        val removePixRequestDto = RemovePixRequestDto(pixId = pixId, clientId = clientId)
        val errors = validator.validate(removePixRequestDto)
        if (errors.isNotEmpty()) throw ConstraintViolationException(errors)

        // COMUNICAR O GRPC
        val grpcRequest = RemoverPixRequest.newBuilder()
            .setPixId(pixId)
            .setClienteId(clientId)
            .build()
        try {
            grpcClient.remover(grpcRequest)
            return HttpResponse.noContent()
        } catch (e: StatusRuntimeException) {
            val statusCode = e.status.code
            val description = e.status.description
            logger.info("Problemas na requisição GRPC")
            logger.info("Status retornado do servidor GRPC: $statusCode")
            logger.info("Mensagem retornada do servidor GRPC: $description")
            GrpcExceptionDispatcher.call(e)
            throw CustomGrpcResponseException(description ?: "Erro inesperado, contate o desenvolvedor.")
        }
    }

    @Error(exception = UnsatisfiedRouteException::class)
    fun customUnsatisfiedBodyRouteResponse(exception: UnsatisfiedRouteException): MutableHttpResponse<CustomErrorBody> {
        logger.info("Tratando a exception UnsatisfiedRouteException")
        return HttpResponse.badRequest(CustomErrorBody(
            statusCode = HttpStatus.BAD_REQUEST.code,
            name = HttpStatus.BAD_REQUEST.name,
            description = "É necessário informar um ${exception.argument.name} no corpo da requisição",
            details = null
        ))
    }
}