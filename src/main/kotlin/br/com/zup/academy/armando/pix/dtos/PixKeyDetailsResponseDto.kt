package br.com.zup.academy.armando.pix.dtos

import br.com.zupedu.armando.BuscarChavePixResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class PixKeyDetailsResponseDto(
    val keyType: String,
    val key: String,
    val account: AccountDetailsResponseDto,
    val createdAt: String
) {
    constructor(grpcResponse: BuscarChavePixResponse) : this(
        keyType = grpcResponse.chavePix.tipoChave.name,
        key = grpcResponse.chavePix.chave,
        account = AccountDetailsResponseDto(
            accountType = grpcResponse.chavePix.conta.tipoConta.name,
            institutionName = grpcResponse.chavePix.conta.instituicaoNome,
            institutionIspb = grpcResponse.chavePix.conta.instituicaoIspb,
            holderName = grpcResponse.chavePix.conta.titularNome,
            holderCpf = grpcResponse.chavePix.conta.titularCpf,
            agency = grpcResponse.chavePix.conta.agencia,
            number = grpcResponse.chavePix.conta.numero
        ),
        createdAt = grpcResponse.chavePix.criadaEm.let {
            LocalDateTime.ofInstant(Instant.ofEpochSecond(it.seconds, it.nanos.toLong()), ZoneOffset.UTC).toString()
        }
    )
}

data class AccountDetailsResponseDto(
    val accountType: String,
    val institutionName: String,
    val institutionIspb: String,
    val holderName: String,
    val holderCpf: String,
    val agency: String,
    val number: String
)
