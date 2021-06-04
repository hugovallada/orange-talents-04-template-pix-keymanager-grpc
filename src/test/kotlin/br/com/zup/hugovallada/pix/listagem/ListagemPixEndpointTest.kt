package br.com.zup.hugovallada.pix.listagem

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
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListagemPixEndpointTest(
    @Inject private val grpcClient: ListPixKeyServiceGrpc.ListPixKeyServiceBlockingStub,
    @Inject private val repository: ChavePixRepository
){

    @Test
    internal fun `deve retornar uma lista com as chaves`() {
        insereBatch()
        val response = grpcClient.listarChaves(IdDoClienteGrpcRequest.newBuilder()
            .setId("c56dfef4-7901-44fb-84e2-a2cefb157890").build())

        assertTrue(response.chavesList.size == 5)
        assertFalse(response.chavesList.isEmpty())
    }

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

        assertTrue(response.chavesList.isEmpty())
    }

    private fun geraChavePix() = ChavePix(
        clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
        tipo = TipoDeChave.CHAVE_ALEATORIA,
        chave = UUID.randomUUID().toString(),
        tipoConta = TipoDeConta.CONTA_CORRENTE,
        conta = Conta(
            instituicao = "ITAU",
            nomeDoTitular = "Hugo",
            cpfDoTitular = "02467781054",
            agencia = "92882",
            numeroDaConta = "722"
        )
    )
    private fun insereBatch(){
        for(i in 1..5){
            val chave = geraChavePix()
            chave.criadaEm = LocalDateTime.now()
            repository.save(chave)
        }
    }


    @Factory
    class GrpcClient(){
        @Singleton
        fun gerarClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListPixKeyServiceGrpc.ListPixKeyServiceBlockingStub? {
            return ListPixKeyServiceGrpc.newBlockingStub(channel)
        }
    }
}