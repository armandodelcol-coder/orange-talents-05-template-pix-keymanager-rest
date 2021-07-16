package br.com.zup.academy.armando.grpc

import br.com.zupedu.armando.PixKeyManagerRegistrarServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class PixManagerClientFactory {
    @Singleton
    fun pixManagerClientStub(@GrpcChannel("pixManager") channel: ManagedChannel): PixKeyManagerRegistrarServiceGrpc.PixKeyManagerRegistrarServiceBlockingStub {
        return PixKeyManagerRegistrarServiceGrpc.newBlockingStub(channel)
    }
}