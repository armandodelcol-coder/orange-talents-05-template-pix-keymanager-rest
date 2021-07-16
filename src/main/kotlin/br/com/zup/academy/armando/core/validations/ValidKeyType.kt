package br.com.zup.academy.armando.core.validations

import br.com.zup.academy.armando.pix.models.KeyType
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidKeyTypeValidator::class])
annotation class ValidKeyType(
    val message: String = "Valor do Enum informado está inválido"
)

@Singleton
class ValidKeyTypeValidator: ConstraintValidator<ValidKeyType, String> {
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidKeyType>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.isNullOrBlank()) return true

        val validValues = KeyType.values().map { keyType -> keyType.name }
        context.messageTemplate("keyType inválido. Valores válidos: $validValues")

        if (validValues.contains(value)) return true

        return false
    }
}
