package br.com.zup.academy.armando.core.handler.helpers

import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.core.handler.CustomErrorDetail
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import javax.validation.ConstraintViolationException

class MakerConstraintViolation {
    companion object {
        fun responseBody(exception: ConstraintViolationException): MutableHttpResponse<CustomErrorBody> {
            val fieldMessages = exception.constraintViolations.map {
                    constraint -> CustomErrorDetail(description = "${constraint.propertyPath.last()} inválido. ${constraint.message}")
            }

            return HttpResponse.badRequest(
                CustomErrorBody(
                HttpStatus.BAD_REQUEST.code,
                name = HttpStatus.BAD_REQUEST.name,
                description = "Alguns campos foram preenchidos indevidamente. Por favor verifique e tente novamente.",
                details = fieldMessages
            ))
        }
    }
}