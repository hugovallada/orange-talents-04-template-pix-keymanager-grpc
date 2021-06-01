package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.conta.DadosContaResponse
import br.com.zup.hugovallada.conta.InstituicaoResponse
import br.com.zup.hugovallada.conta.TitularResponse
import br.com.zup.hugovallada.externo.ItauERPClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixEndpointTest(
    private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val erpClient: ItauERPClient
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @ParameterizedTest
    @CsvSource(
        "CONTA_CORRENTE, EMAIL, email@email.com", "CONTA_POUPANCA, TELEFONE_CELULAR, +5516999999999",
        "CONTA_CORRENTE, CHAVE_ALEATORIA, ''", "CONTA_POUPANCA, CPF, 44444444444"
    )
    internal fun `novo usuario deve ser cadastrado caso os dados sejam validos`(
        conta: String,
        chave: String,
        valor: String
    ) {
        repository.deleteAll()
        val request = CadastraChavePixGrpcRequest.newBuilder()
            .setIdCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setValorChave(valor)
            .setTipoDeChave(TipoDeChave.valueOf(chave))
            .setTipoDeConta(TipoDeConta.valueOf(conta)).build()

        Mockito.`when`(erpClient.buscarClientePorConta(request.idCliente, request.tipoDeConta.name))
            .thenReturn(gerarDadosContaResponse())

        //acao
        val response = grpcClient.cadastrarChave(request)


        // validação
        with(response){
            assertNotNull(this)
            assertNotNull(id)
            Thread.sleep(1000)
            assertTrue(repository.existsById(UUID.fromString(id)))
        }

    }

//    @Test
//    internal fun `deve cadastrar no banco quando os dados forem validos e retornar o id interno`() {
//        //cenario
//        val request = CadastraChavePixGrpcRequest.newBuilder()
//            .setIdCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
//            .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
//            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE).build()
//
//        Mockito.`when`(erpClient.buscarClientePorConta(request.idCliente, request.tipoDeConta.name))
//            .thenReturn(gerarDadosContaResponse())
//
//        //acao
//        val response = grpcClient.cadastrarChave(request)
//
//
//        // validação
//        assertNotNull(response)
//        assertNotNull(response.id)
//        Thread.sleep(1000)
//        assertTrue(repository.existsById(UUID.fromString(response.id)))
//    }

    @Test
    internal fun `deve retornar o status ALREADY EXISTS quando tentar cadastrar uma chave que ja existe`() {
        // cenário
        val chave = ChavePix(
            clienteId = UUID.randomUUID(),
            tipo = TipoDeChave.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoDeConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "ITAU",
                nomeDoTitular = "Hugo",
                cpfDoTitular = "029300292",
                agencia = "92882",
                numeroDaConta = "722"
            )
        )
        repository.save(chave)

        assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(
                CadastraChavePixGrpcRequest.newBuilder()
                    .setIdCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setValorChave("email@email.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE).build()
            )
        }.run {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Essa chave já está cadastrada", status.description)
        }
    }

    @Test
    internal fun `deve retornar um status NOT FOUND quando o id do cliente nao for encontrado no sistema externo`() {
        val request = CadastraChavePixGrpcRequest.newBuilder()
            .setIdCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE).build()

        Mockito.`when`(erpClient.buscarClientePorConta(request.idCliente, request.tipoDeConta.name))
            .thenReturn(null)

        assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("O cliente não foi encontrado", status.description)
        }

    }

    @ParameterizedTest
    @CsvSource(
        "5260263c-a3c1-4727-ae32-3bdb2538841b, CONTA_CORRENTE, TELEFONE_CELULAR , email@email.com",
        "52602c-a3c1-4727-ae32-3bdb2538841b,CONTA_POUPANCA, TELEFONE_CELULAR, +5516999999999",
        "5260263c-a3c1-4727-ae32-3bdb2538841b, CONTA_CORRENTE, CPF,email@email.com ",
        "5260263c-a3c1-4727-ae32-3bdb2538841b,DESCONHECIDA, CPF, 44444444444",
        "5260263c-a3c1-4727-ae32-3bdb2538841b, CONTA_POUPANCA, CPF, ''",
        "5260263c-a3c1-4727-ae32-3bdb2538841b, CONTA_CORRENTE, DESCONHECIDO, email@email",
    )
    internal fun `deve retornar invalid argument quando os dados de entrada forem invalidos`(
        id: String, conta: String, chave: String, valor: String
    ) {
        val request = CadastraChavePixGrpcRequest.newBuilder()
            .setIdCliente(id)
            .setValorChave(valor)
            .setTipoDeChave(TipoDeChave.valueOf(chave))
            .setTipoDeConta(TipoDeConta.valueOf(conta)).build()

        assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar uma status UNKNOW quando um problema aconteca no client`() {
        val request = CadastraChavePixGrpcRequest.newBuilder()
            .setIdCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE).build()
        Mockito.`when`(erpClient.buscarClientePorConta(request.idCliente, request.tipoDeConta.name))
            .thenThrow(RuntimeException())

        assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChave(request)
        }.run {
            assertEquals(Status.UNKNOWN.code, status.code)
        }
    }

    private fun gerarDadosContaResponse(): DadosContaResponse {
        return DadosContaResponse(
            tipo = TipoDeConta.CONTA_CORRENTE.name,
            instituicao = InstituicaoResponse(nome = "Itau", ispb = "109232"),
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
class GrpcClient {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
        return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    }

}