package br.com.zup.academy.armando.pix.controllers

import br.com.zup.academy.armando.core.handler.CustomErrorBody
import br.com.zup.academy.armando.grpc.PixManagerClientFactory
import br.com.zup.academy.armando.pix.dtos.PixKeyDetailsResponseDto
import br.com.zupedu.armando.*
import com.google.protobuf.Timestamp
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
import javax.inject.Singleton

@MicronautTest
internal class PixKeyDetailsControllerTest(
    private val controller: PixKeyDetailsController,
    private val grpcClient: PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceBlockingStub
) {
    @Factory
    @Replaces(factory = PixManagerClientFactory::class)
    class MockitoPixManagerDetailsClient {
        @Singleton
        fun stubMock() = Mockito.mock(PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceBlockingStub::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(grpcClient)
    }

    @Test
    fun `deve retornar um erro quando pixId for inválido`() {
        // action
        val response = controller.getPixKey(pixId = "123", clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b")
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
        val response = controller.getPixKey(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "b2538841b")
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
        val request = BuscarChavePixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.UNAVAILABLE)
        Mockito.`when`(grpcClient.buscar(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.getPixKey(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
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
        val request = BuscarChavePixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.INTERNAL.withDescription("Dummy Description"))
        Mockito.`when`(grpcClient.buscar(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.getPixKey(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
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
        val request = BuscarChavePixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        val dummyResponseErrorGrpc = StatusRuntimeException(Status.NOT_FOUND.withDescription("chave pix não encontrada"))
        Mockito.`when`(grpcClient.buscar(request)).thenThrow(dummyResponseErrorGrpc)
        // action
        val response = controller.getPixKey(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
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
    fun `deve exibir os detalhes de uma chave pix`() {
        // scenario
        val request = BuscarChavePixRequest.newBuilder()
            .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setClienteId("37e2451c-470b-4043-94f2-f46796cdbfc2")
            .build()
        Mockito.`when`(grpcClient.buscar(request)).thenReturn(
            BuscarChavePixResponse.newBuilder()
                .setChavePix(
                    BuscarChavePixResponse.ChavePix.newBuilder()
                        .setTipoChave(TipoChave.EMAIL)
                        .setChave("email@test.com")
                        .setConta(BuscarChavePixResponse.ChavePix.Conta.newBuilder()
                            .setTipoConta(TipoConta.CONTA_CORRENTE)
                            .setInstituicaoNome("FAKE")
                            .setInstituicaoIspb("123")
                            .setAgencia("321")
                            .setNumero("123456")
                            .setTitularNome("Dr. Mario")
                            .setTitularCpf("11111111111"))
                        .setCriadaEm(Timestamp.newBuilder().setNanos(13454564).setSeconds(5454548786545))
                ).build()
        )
        // action
        val response = controller.getPixKey(pixId = "5260263c-a3c1-4727-ae32-3bdb2538841b", clientId = "37e2451c-470b-4043-94f2-f46796cdbfc2")
        // verification
        with (response) {
            assertEquals(HttpStatus.OK.code, status.code)
            assertEquals(PixKeyDetailsResponseDto::class.java, body().javaClass)
        }
    }
}