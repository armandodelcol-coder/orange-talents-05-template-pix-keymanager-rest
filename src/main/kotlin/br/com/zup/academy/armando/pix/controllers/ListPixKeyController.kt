package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.dispatchers.GrpcExceptionDispatcher
import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.core.handler.CustomErrorHandler
import br.com.zup.academy.armando.core.handler.exceptions.CustomGrpcResponseException
import br.com.zup.academy.armando.core.handler.helpers.MakerConstraintViolation
import br.com.zup.academy.armando.core.validations.ValidUUID
import br.com.zup.academy.armando.pix.dtos.PixKeyResponseToListDto
import br.com.zupedu.armando.ListarPixRequest
import br.com.zupedu.armando.PixKeyManagerListarServiceGrpc
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Controller
@Validated
@CustomErrorHandler
class ListPixKeyController(
    private val grpcClient: PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceBlockingStub
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Get("/api/v1/clients/{clientId}/pix")
    fun listPixKeys(@PathVariable @ValidUUID clientId: String): MutableHttpResponse<List<PixKeyResponseToListDto>> {
        val grpcRequest = ListarPixRequest.newBuilder().setClienteId(clientId).build()
        try {
            val grpcResponse = grpcClient.listar(grpcRequest)
            return HttpResponse.ok(grpcResponse.chavesList.map(::PixKeyResponseToListDto))
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

    @Error
    fun customConstraintViolationBodyResponse(exception: ConstraintViolationException): MutableHttpResponse<CustomErrorBody> {
        return MakerConstraintViolation.responseBody(exception)
    }
}