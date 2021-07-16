package br.com.zup.academy.armando.core.validations

import br.com.zup.academy.armando.pix.models.AccountType
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidAccountTypeValidator::class])
annotation class ValidAccountType(
    val message: String = "Valor do Enum informado está inválido"
)

@Singleton
class ValidAccountTypeValidator: ConstraintValidator<ValidAccountType, String> {
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidAccountType>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.isNullOrBlank()) return true

        val validValues = AccountType.values().map { keyType -> keyType.name }
        context.messageTemplate("accountType inválido. Valores válidos: $validValues")

        if (validValues.contains(value)) return true

        return false
    }
}
