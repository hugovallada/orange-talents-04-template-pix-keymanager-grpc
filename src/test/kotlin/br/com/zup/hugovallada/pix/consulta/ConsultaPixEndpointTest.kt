package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.DadosDeConsultaGrpcInternoRequest
import br.com.zup.hugovallada.DadosDeConsultaGrpcInternoRequestOrBuilder
import br.com.zup.hugovallada.SearchKeyServiceGrpc
import br.com.zup.hugovallada.pix.ChavePixRepository
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
internal class ConsultaPixEndpointTest(
    @Inject private val grpcClient: SearchKeyServiceGrpc.SearchKeyServiceBlockingStub
){


    @Test
    internal fun `deve retornar status not found quando o id pix nao for encontrado`() {
        val request = DadosDeConsultaGrpcInternoRequest.newBuilder()
            .setIdCliente(UUID.randomUUID().toString())
            .setIdPix(UUID.randomUUID().toString()).build()

        assertThrows<StatusRuntimeException>{
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave pix de id ${request.idPix} não foi encontrada", status.description)
        }
    }

    @Factory
    class GrpcClient(){
        @Singleton
        fun geraClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): SearchKeyServiceGrpc.SearchKeyServiceBlockingStub? {
            return SearchKeyServiceGrpc.newBlockingStub(channel)
        }

    }
}