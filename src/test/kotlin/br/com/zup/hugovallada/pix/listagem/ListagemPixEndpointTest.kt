package br.com.zup.hugovallada.pix.listagem

import br.com.zup.hugovallada.IdDoClienteGrpcRequest
import br.com.zup.hugovallada.ListPixKeyServiceGrpc
import br.com.zup.hugovallada.ListaPixGrpcResponse
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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListagemPixEndpointTest(
    @Inject private val grpcClient: ListPixKeyServiceGrpc.ListPixKeyServiceBlockingStub
){

    @Test
    internal fun `deve retornar um status INVALID ARGUMENT quando tentar passar um id nulo ou nao valido`() {

        assertThrows<StatusRuntimeException>{
            grpcClient.listarChaves(IdDoClienteGrpcRequest.newBuilder()
                .setId("email").build())
        }.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O id é nulo ou não é uma UUID", status.description)
        }
    }

    @Test
    internal fun `deve retornar uma lista vazia quando o usuario nao possuir uma chave`() {
        val response = grpcClient.listarChaves(IdDoClienteGrpcRequest.newBuilder()
            .setId(UUID.randomUUID().toString()).build())

        assertTrue(response.chavePixList.isEmpty())
    }

    @Factory
    class GrpcClient(){
        @Singleton
        fun gerarClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListPixKeyServiceGrpc.ListPixKeyServiceBlockingStub? {
            return ListPixKeyServiceGrpc.newBlockingStub(channel)
        }
    }
}