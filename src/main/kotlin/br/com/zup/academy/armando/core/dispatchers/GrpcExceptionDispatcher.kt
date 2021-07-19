package br.com.zup.academy.armando.core.dispatchers

import br.com.zup.academy.armando.core.handler.exceptions.GrpcNotFoundException
import br.com.zup.academy.armando.core.handler.exceptions.GrpcUnavailableException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory

class GrpcExceptionDispatcher {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        fun call(e: StatusRuntimeException) {
            logger.info("Verificando e tratando erros do GRPC")
            val statusCode = e.status.code
            val description = e.status.description
            if (statusCode == Status.UNAVAILABLE.code) throw GrpcUnavailableException("Servidor GRPC indisponível")

            if (statusCode == Status.NOT_FOUND.code) throw GrpcNotFoundException(description!!)
        }
    }
}