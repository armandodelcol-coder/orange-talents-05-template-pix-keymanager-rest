package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.grpc.PixManagerClientFactory
import br.com.zup.academy.armando.pix.dtos.PixKeyResponseToListDto
import br.com.zupedu.armando.ListarPixRequest
import br.com.zupedu.armando.ListarPixResponse
import br.com.zupedu.armando.PixKeyManagerListarServiceGrpc
import br.com.zupedu.armando.TipoConta
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class ListPixKeyControllerTest(
    private val grpcClient: PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceBlockingStub,
    private val controller: ListPixKeyController
) {
    @field:Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Factory
    @Replaces(factory = PixManagerClientFactory::class)
    class MockitoPixManagerListClient {
        @Singleton
        fun stubMock() = Mockito.mock(PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceBlockingStub::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(grpcClient)
    }

    @Test
    fun `deve retornar um erro quando clientId for inválido`() {
        // scenario
        val request = HttpRequest.GET<Any>("/api/v1/clients/123/pix")
        // action
        val error = org.junit.jupiter.api.assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, String::class.java)
        }
        val response = error.response
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body().toString().contains("clientId inválido"))
        }
    }

    @Test
    fun `deve retornar um erro quando servidor Grpc estiver indisponível`() {
        // scenario
        val request = ListarPixRequest.newBuilder()
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.UNAVAILABLE)
        Mockito.`when`(grpcClient.listar(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.listPixKeys(clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, status.code)
            assertEquals(
                "Servidor GRPC indisponível",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    @Test
    fun `deve retornar um erro de regra de negócio quando der um erro na chamada Grpc`() {
        // scenario
        val request = ListarPixRequest.newBuilder()
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.INTERNAL.withDescription("Dummy Description"))
        Mockito.`when`(grpcClient.listar(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.listPixKeys(clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(io.micronaut.http.HttpStatus.UNPROCESSABLE_ENTITY.code, status.code)
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
    fun `deve listar as chaves pix`() {
        // scenario
        val request = ListarPixRequest.newBuilder()
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        Mockito.`when`(grpcClient.listar(request)).thenReturn(
            ListarPixResponse.newBuilder().addChaves(
                ListarPixResponse.ChavePixListagemResponse.newBuilder()
                    .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setChave("email@email.com")
                    .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
                    .setCriadaEm(LocalDateTime.now().toString())
            ).build()
        )
        // action
        val response = controller.listPixKeys(clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(HttpStatus.OK.code, status.code)
            assertTrue(body.isPresent)
            assertEquals("email@email.com", body.get()[0].key)
        }
    }
}