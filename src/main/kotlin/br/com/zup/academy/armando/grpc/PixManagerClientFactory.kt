package br.com.zup.academy.armando.grpc

import br.com.zupedu.armando.PixKeyManagerBuscarServiceGrpc
import br.com.zupedu.armando.PixKeyManagerListarServiceGrpc
import br.com.zupedu.armando.PixKeyManagerRegistrarServiceGrpc
import br.com.zupedu.armando.PixKeyManagerRemoverServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class PixManagerClientFactory {
    @Singleton
    fun pixManagerClientRegistrarStub(@GrpcChannel("pixManager") channel: ManagedChannel): PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub {
        return PixKeyManagerRegistrarServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun pixManagerClientRemoverStub(@GrpcChannel("pixManager") channel: ManagedChannel): PixKeyManagerRemoverServiceGrpc.PixKeyManagerRemoverServiceBlockingStub {
        return PixKeyManagerRemoverServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun pixManagerClientBuscarStub(@GrpcChannel("pixManager") channel: ManagedChannel): PixKeyManagerBuscarServiceGrpc.PixKeyManagerBuscarServiceBlockingStub {
        return PixKeyManagerBuscarServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun pixManagerClientListarStub(@GrpcChannel("pixManager") channel: ManagedChannel): PixKeyManagerListarServiceGrpc.PixKeyManagerListarServiceBlockingStub {
        return PixKeyManagerListarServiceGrpc.newBlockingStub(channel)
    }
}