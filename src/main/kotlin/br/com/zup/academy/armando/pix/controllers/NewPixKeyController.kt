package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.dispatchers.GrpcExceptionDispatcher
import br.com.zup.academy.armando.core.handler.CustomErrorHandler
import br.com.zup.academy.armando.core.handler.exceptions.CustomGrpcResponseException
import br.com.zup.academy.armando.pix.dtos.NewPixKeyRequest
import br.com.zup.academy.armando.pix.models.KeyType
import br.com.zupedu.armando.PixKeyManagerRegistrarServiceGrpc
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.uri.UriBuilder
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Controller
@Validated
@CustomErrorHandler
class NewPixKeyController(
    private val validator: Validator,
    private val grpcClient: PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Post("/api/v1/pix")
    fun register(@Body newPixKeyRequest: NewPixKeyRequest): HttpResponse<Any> {
        val errors = validator.validate(newPixKeyRequest)
        if (errors.isNotEmpty()) throw ConstraintViolationException(errors)

        KeyType.valueOf(newPixKeyRequest.keyType).validate(newPixKeyRequest.key)
        // FAZER A COMUNICAÇÃO GRPC
        val grpcRequest = newPixKeyRequest.toGrpcRequest()
        try {
            val grpcResponse = grpcClient.registrar(grpcRequest)
            return HttpResponse.created<Any?>(UriBuilder.of("/api/v1/pix/{pixId}")
                .expand(mutableMapOf(Pair("pixId", grpcResponse.pixId))))
                .body(object {val pixId = grpcResponse.pixId})
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

    // PODERIA TRATAR O UnsatisfiedBodyRouteException
    // para uma mensagem mais amigável quando não informar algum campo necessário no Body.
}