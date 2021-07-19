package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.grpc.PixManagerClientFactory
import br.com.zupedu.armando.PixKeyManagerRemoverServiceGrpc
import br.com.zupedu.armando.RemoverPixRequest
import br.com.zupedu.armando.RemoverPixResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class RemovePixKeyControllerTest(
    private val controller: RemovePixKeyController,
    private val grpcClient: PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceBlockingStub
) {
    @field:Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Factory
    @Replaces(factory = PixManagerClientFactory::class)
    class MockitoPixManagerRemoveClient {
        @Singleton
        fun stubMock() = Mockito.mock(PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceBlockingStub::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(grpcClient)
    }

    @Test
    fun `deve retornar um erro quando não informar o clientId`() {
        // scenario
        val request = HttpRequest.DELETE<Any>("/api/v1/pix/5260263c-a3c1-4727-ae32-3bdb2538841b")
        // action
        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, String::class.java)
        }
        val response = error.response
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body().toString().contains("É necessário informar um clientId no corpo da requisição"))
        }
    }

    @Test
    fun `deve retornar um erro quando pixId for inválido`() {
        // action
        val response = controller.removePix(pixId = "123", clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b")
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals("pixId inválido. não é um formato válido de UUID", (response.body() as CustomErrorBody).details!![0].description)
        }
    }

    @Test
    fun `deve retornar um erro quando clientId for inválido`() {
        // action
        val response = controller.removePix(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "b2538841b")
        // verification
        with (response) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals("clientId inválido. não é um formato válido de UUID", (response.body() as CustomErrorBody).details!![0].description)
        }
    }

    @Test
    fun `deve retornar um erro quando servidor Grpc estiver indisponível`() {
        // scenario
        val request = RemoverPixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.UNAVAILABLE)
        Mockito.`when`(grpcClient.remover(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.removePix(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "Servidor GRPC indisponível",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    @Test
    fun `deve retornar um erro de regra de negócio quando der um erro na chamada Grpc`() {
        // scenario
        val request = RemoverPixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.INTERNAL.withDescription("Dummy Description"))
        Mockito.`when`(grpcClient.remover(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.removePix(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
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

    @Test
    fun `deve retornar um erro quando pixId não for encontrado no Grpc`() {
        // scenario
        val request = RemoverPixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.NOT_FOUND.withDescription("chave pix não encontrada"))
        Mockito.`when`(grpcClient.remover(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.removePix(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
            assertTrue(response.body() is CustomErrorBody)
            assertEquals(
                "chave pix não encontrada",
                (response.body() as CustomErrorBody).description
            )
        }
    }

    // HAPPY PATH
    @Test
    fun `deve excluir uma chave pix`() {
        // scenario
        val request = RemoverPixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        Mockito.`when`(grpcClient.remover(request)).thenReturn(RemoverPixResponse.newBuilder().build())
        // action
        val response = controller.removePix(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(HttpStatus.NO_CONTENT.code, status.code)
        }
    }
}