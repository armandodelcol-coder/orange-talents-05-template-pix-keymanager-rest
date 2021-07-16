package br.com.zup.academy.armando.pix.dtos

import br.com.zup.academy.armando.core.validations.ValidAccountType
import br.com.zup.academy.armando.core.validations.ValidKeyType
import br.com.zup.academy.armando.core.validations.ValidUUID
import br.com.zupedu.armando.RegistrarPixRequest
import br.com.zupedu.armando.TipoChave
import br.com.zupedu.armando.TipoConta
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class NewPixKeyRequest(
    @field:NotBlank
    @field:ValidUUID
    val clientId: String,

    @field:NotBlank
    @field:ValidKeyType
    val keyType: String,

    val key: String?,

    @field:NotBlank
    @field:ValidAccountType
    val accountType: String
) {
    fun toGrpcRequest(): RegistrarPixRequest {
        return RegistrarPixRequest.newBuilder()
            .setClienteId(clientId)
            .setTipoChave(TipoChave.valueOf(keyType))
            .setChave(key ?: "")
            .setTipoConta(TipoConta.valueOf(accountType))
            .build()
    }
}
