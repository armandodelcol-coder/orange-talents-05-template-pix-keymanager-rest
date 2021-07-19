package br.com.zup.academy.armando.pix.dtos

import br.com.zup.academy.armando.core.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemovePixRequestDto(
    @field:NotBlank @field:ValidUUID val pixId: String,
    @field:NotBlank @field:ValidUUID val clientId: String
)
