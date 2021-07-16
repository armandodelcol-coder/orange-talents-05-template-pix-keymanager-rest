package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.grpc.PixManagerClientFactory
import br.com.zup.academy.armando.pix.dtos.NewPixKeyRequest
import br.com.zupedu.armando.PixKeyManagerRegistrarServiceGrpc
import br.com.zupedu.armando.RegistrarPixResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest
internal class NewPixKeyControllerTest(
    private val newPixKeyController: NewPixKeyController,
    private val grpcClient: PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub
) {
    @Factory
    @Replaces(factory = PixManagerClientFactory::class)
    class MockitoPixManagerRegisterClient {
        @Singleton
        fun stubMock() = Mockito.mock(PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub::class.java)
    }

    val dummyGoodRequestDto = NewPixKeyRequest(
        clientId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
        keyType = "EMAIL",
        key = "armando@teste.com",
        accountType = "CONTA_CORRENTE"
    )

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(grpcClient)
    }

    @Test
    fun `deve retornar uma bad request quando clientId não for um UUID válido`() {
        // scenario
        val request = NewPixKeyRequest(
            "123",
            dummyGoodRequestDto.keyType,
            dummyGoodRequestDto.key,
            dummyGoodRequestDto.accountType
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals("não é um formato válido de UUID", (response.body() as CustomErrorBody).details!![0].description)
        }
    }

    @Test
    fun `deve retornar uma bad request quando keyType não for um tipo válido`() {
        // scenario
        val request = NewPixKeyRequest(
            dummyGoodRequestDto.clientId,
            "INVALIDO",
            dummyGoodRequestDto.key,
            dummyGoodRequestDto.accountType
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertTrue((response.body() as CustomErrorBody).details!![0].description.contains("keyType inválido"))
        }
    }

    @Test
    fun `deve retornar uma bad request quando accountType não for um tipo válido`() {
        // scenario
        val request = NewPixKeyRequest(
            dummyGoodRequestDto.clientId,
            dummyGoodRequestDto.keyType,
            dummyGoodRequestDto.key,
            "INVALIDO"
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertTrue((response.body() as CustomErrorBody).details!![0].description.contains("accountType inválido"))
        }
    }

    @Test
    fun `deve retornar uma bad request quando keyType for CPF e a key for um cpf inválido`() {
        // scenario
        val request = NewPixKeyRequest(
            dummyGoodRequestDto.clientId,
            "CPF",
            "123456789",
            dummyGoodRequestDto.accountType
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "key é obrigatória e formato esperado deve ser um CPF válido.",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    @Test
    fun `deve retornar uma bad request quando keyType for EMAIL e a key for um email inválido`() {
        // scenario
        val request = NewPixKeyRequest(
            dummyGoodRequestDto.clientId,
            "EMAIL",
            "123456789",
            dummyGoodRequestDto.accountType
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "key é obrigatória e formato esperado deve ser seu@email.com",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    @Test
    fun `deve retornar uma bad request quando keyType for CELULAR e a key for um celular inválido`() {
        // scenario
        val request = NewPixKeyRequest(
            dummyGoodRequestDto.clientId,
            "CELULAR",
            "123456789",
            dummyGoodRequestDto.accountType
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "key é obrigatória e formato esperado deve ser +5585988714077",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    @Test
    fun `deve retornar uma bad request quando keyType for RANDOMICA e a key for informada`() {
        // scenario
        val request = NewPixKeyRequest(
            dummyGoodRequestDto.clientId,
            "RANDOMICA",
            "123456789",
            dummyGoodRequestDto.accountType
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "Para keyType RANDOMICA não deve ser informada uma key.",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    @Test
    fun `deve retornar um erro de regra de negócio quando der um erro na chamada Grpc`() {
        // scenario
        val request = dummyGoodRequestDto
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.INTERNAL.withDescription("Dummy Description"))
        Mockito.`when`(grpcClient.registrar(request.toGrpcRequest())).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "Erro ao comunicar o gerenciador de chave Pix",
                (response.body() as CustomErrorBody).description
            )
            assertEquals(
                "Dummy Description",
                (response.body() as CustomErrorBody).details!![0].description
            )
        }
    }

    // HAPPY PATH
    @Test
    fun `deve cadastrar uma chavePix com sucesso e retornar o pixId`() {
        // scenario
        val request = dummyGoodRequestDto
        val dummyPixId = UUID.randomUUID().toString()
        Mockito.`when`(grpcClient.registrar(request.toGrpcRequest())).thenReturn(
            RegistrarPixResponse.newBuilder()
                .setPixId(dummyPixId)
                .build()
        )
        // action
        val response = newPixKeyController.register(request)
        // verification
        with (response) {
            assertEquals(HttpStatus.CREATED.code, status.code)
            assertTrue(response.headers["Location"].contains(dummyPixId))
        }
    }
}