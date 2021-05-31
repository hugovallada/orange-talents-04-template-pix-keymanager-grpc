package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.conta.DadosContaResponse
import br.com.zup.hugovallada.conta.InstituicaoResponse
import br.com.zup.hugovallada.conta.TitularResponse
import br.com.zup.hugovallada.externo.ItauERPClient
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixEndpointTest(
    private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val erpClient: ItauERPClient
) {





    @Test
    internal fun `deve cadastrar no banco quando os dados forem validos e retornar o id interno`() {
        //cenario
        val request = CadastraChavePixGrpcRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb15789")
            .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE).build()

        Mockito.`when`(erpClient.buscarClientePorConta(request.idCliente, request.tipoDeConta.name))
            .thenReturn(gerarDadosContaResponse())

        //acao
        val response = grpcClient.cadastrarChave(request)


        // validação
        assertNotNull(response)
        assertNotNull(response.id)
        Thread.sleep(1000)
        assertTrue(repository.existsById(UUID.fromString(response.id)))
    }


    private fun gerarDadosContaResponse(): DadosContaResponse{
        return DadosContaResponse(
            tipo = TipoDeConta.CONTA_CORRENTE.name,
            instituicao = InstituicaoResponse(nome = "Itau",ispb = "109232"),
            agencia = "02932",
            numero = "8239",
            titular = TitularResponse(nome = "Hugo", "8273282")
        )
    }

    @MockBean(ItauERPClient::class)
    fun mockItauErpClient(): ItauERPClient? {
        return Mockito.mock(ItauERPClient::class.java)
    }



}

@Factory
class GrpcClient{
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub{
        return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    }

}