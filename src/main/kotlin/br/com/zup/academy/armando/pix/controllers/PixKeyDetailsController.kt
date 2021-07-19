package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.dispatchers.GrpcExceptionDispatcher
import br.com.zup.academy.armando.core.handler.CustomErrorHandler
import br.com.zup.academy.armando.core.handler.exceptions.CustomGrpcResponseException
import br.com.zup.academy.armando.pix.dtos.PixKeyDetailsRequestDto
import br.com.zup.academy.armando.pix.dtos.PixKeyDetailsResponseDto
import br.com.zupedu.armando.BuscarChavePixRequest
import br.com.zupedu.armando.PixKeyManagerBuscarServiceGrpc
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Controller
@CustomErrorHandler
class PixKeyDetailsController(
    private val grpcClient: PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceBlockingStub,
    private val validator: Validator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Get("/api/v1/clients/{clientId}/pix/{pixId}")
    fun getPixKey(
        @PathVariable pixId: String,
        @PathVariable clientId: String
    ): HttpResponse<Any> {
        val pixDetailsRequestDto = PixKeyDetailsRequestDto(pixId = pixId, clientId = clientId)
        val errors = validator.validate(pixDetailsRequestDto)
        if (errors.isNotEmpty()) throw ConstraintViolationException(errors)

        // COMUNICAR O GRPC
        val grpcRequest = BuscarChavePixRequest.newBuilder()
            .setPixId(pixId)
            .setClienteId(clientId)
            .build()
        try {
            val response = grpcClient.buscar(grpcRequest)
            return HttpResponse.ok(PixKeyDetailsResponseDto(response))
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
}