package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.DeletarChavePixGrpcRequest
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletarChavePixEndpointTest(
    @Inject private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {

    @Test
    internal fun `deve lancar um status NOT FOUND quando o idPix nao for encontrado`() {

        assertThrows<StatusRuntimeException>{
            grpcClient.deletarChave(DeletarChavePixGrpcRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890").setIdPix("5260263c-a9c1-4727-ae32-3bdb2538841b").build())
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave Pix não foi encontrada", status.description)
        }

    }
}


