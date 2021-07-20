package br.com.zup.academy.armando.pix.dtos

import br.com.zupedu.armando.ListarPixResponse

data class PixKeyResponseToListDto(
    val pixId: String,
    val clientId: String,
    val keyType: String,
    val key: String,
    val accountType: String,
    val createdAt: String
) {
    constructor(grpcResponse: ListarPixResponse.ChavePixListagemResponse) : this(
        pixId = grpcResponse.pixId,
        clientId = grpcResponse.clienteId,
        keyType = grpcResponse.tipoChave.name,
        key = grpcResponse.chave,
        accountType = grpcResponse.tipoConta.name,
        createdAt = grpcResponse.criadaEm
    )
}
