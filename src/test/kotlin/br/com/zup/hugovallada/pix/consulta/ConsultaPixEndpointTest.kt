package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.pix.ChavePix
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
    @Inject private val repository: ChavePixRepository,
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

    @Test
    internal fun `deve retornar permission denied quando a chave id nao pertencer ao cliente`() {
        val chavePix = geraChavePix()
        repository.save(chavePix)
        val request = DadosDeConsultaGrpcInternoRequest.newBuilder()
            .setIdCliente(UUID.randomUUID().toString())
            .setIdPix(chavePix.id.toString()).build()

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("A chave que você está tentando consultar, não existe ou não lhe pertence", status.description)
        }
    }

    @Factory
    class GrpcClient(){
        @Singleton
        fun geraClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): SearchKeyServiceGrpc.SearchKeyServiceBlockingStub? {
            return SearchKeyServiceGrpc.newBlockingStub(channel)
        }

    }

    private fun geraChavePix() = ChavePix(
        clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
        tipo = TipoDeChave.EMAIL,
        chave = "email@email.com",
        tipoConta = TipoDeConta.CONTA_CORRENTE,
        conta = Conta(
            instituicao = "ITAU",
            nomeDoTitular = "Hugo",
            cpfDoTitular = "02467781054",
            agencia = "92882",
            numeroDaConta = "722"
        )
    )
}