package br.com.zup.academy.armando.core.handler

import br.com.zup.academy.armando.core.handler.exceptions.CustomBadRequestException
import br.com.zup.academy.armando.core.handler.exceptions.CustomGrpcResponseException
import br.com.zup.academy.armando.core.handler.exceptions.GrpcNotFoundException
import br.com.zup.academy.armando.core.handler.exceptions.GrpcUnavailableException
import br.com.zup.academy.armando.core.handler.helpers.MakerConstraintViolation
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(CustomErrorHandler::class)
class CustomErrorHandlerInterceptor: MethodInterceptor<Any, Any> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: Exception) {
            when(ex) {
                is ConstraintViolationException -> {
                    return MakerConstraintViolation.responseBody(ex)
                }
                is CustomBadRequestException -> {
                    return HttpResponse.badRequest(CustomErrorBody(
                        statusCode = HttpStatus.BAD_REQUEST.code,
                        name = HttpStatus.BAD_REQUEST.name,
                        description = ex.message,
                        details = null
                    ))
                }
                is CustomGrpcResponseException -> {
                    return HttpResponseFactory.INSTANCE.status(HttpStatus.UNPROCESSABLE_ENTITY,
                        CustomErrorBody(
                            statusCode = HttpStatus.UNPROCESSABLE_ENTITY.code,
                            name = HttpStatus.UNPROCESSABLE_ENTITY.name,
                            description = "Erro ao comunicar o gerenciador de chave Pix",
                            details = listOf(CustomErrorDetail(description = ex.message))
                        )
                    )
                }
                is GrpcUnavailableException -> {
                    return HttpResponse.serverError(CustomErrorBody(
                        statusCode = HttpStatus.INTERNAL_SERVER_ERROR.code,
                        name = HttpStatus.INTERNAL_SERVER_ERROR.name,
                        description = ex.message,
                        details = null
                    ))
                }
                is GrpcNotFoundException -> {
                    return HttpResponse.notFound(CustomErrorBody(
                        statusCode = HttpStatus.NOT_FOUND.code,
                        name = HttpStatus.NOT_FOUND.name,
                        description = ex.message,
                        details = null
                    ))
                }
                else -> {
                    logger.info("ERRO INESPERADO")
                    logger.error(ex.stackTraceToString())
                    return HttpResponse.serverError(CustomErrorBody(
                        statusCode = HttpStatus.INTERNAL_SERVER_ERROR.code,
                        name = HttpStatus.INTERNAL_SERVER_ERROR.name,
                        description = "Ooops, erro inesperado. Por favor comunique o desenvolvedor.",
                        details = null
                    ))
                }
            }
        }

        return null
    }
}