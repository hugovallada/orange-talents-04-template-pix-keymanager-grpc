package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.conta.DadosContaResponse
import br.com.zup.hugovallada.conta.InstituicaoResponse
import br.com.zup.hugovallada.conta.TitularResponse
import br.com.zup.hugovallada.externo.DadosClienteResponseClient
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
internal class ChavePixEndpointTest(
    private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val erpClient: ItauERPClient
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    // Teste de cadastro

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


    @Test
    internal fun `deve retornar o status ALREADY EXISTS quando tentar cadastrar uma chave que ja existe`() {
        // cenário
        val chave = geraChavePix()
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

    // Teste de Remoção
    @Test
    internal fun `deve lancar um status NOT FOUND quando o idPix nao for encontrado`() {

        assertThrows<StatusRuntimeException>{
            grpcClient.deletarChave(
                DeletarChavePixGrpcRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890").setIdPix("5260263c-a9c1-4727-ae32-3bdb2538841b").build())
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave Pix não foi encontrada", status.description)
        }

    }


    @Test
    internal fun `deve retornar um status UNKNOW quando um erro acontecer do lado do client`() {
        val chave = geraChavePix()
        repository.save(chave)
        //cenario
        Mockito.`when`(erpClient.buscarClientePorId("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenThrow(java.lang.RuntimeException())

        assertThrows<StatusRuntimeException> {
            grpcClient.deletarChave(
                DeletarChavePixGrpcRequest.newBuilder().setIdPix(chave.id.toString())
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890").build())
        }.run {
            assertEquals(Status.UNKNOWN.code, status.code)
        }
    }


    @Test
    internal fun `deve deletar uma chave e retornar um status true`(){
        val chave = geraChavePix()
        repository.save(chave)

        Mockito.`when`(erpClient.buscarClientePorId("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(DadosClienteResponseClient("c56dfef4-7901-44fb-84e2-a2cefb157890","Hugo", "02467781054",
            InstituicaoResponse("Itau","929292")
            ))

        val response = grpcClient.deletarChave(
            DeletarChavePixGrpcRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setIdPix(chave.id.toString()).build()
        )

        assertEquals(true, response.sucesso)
        assertFalse(repository.existsById(chave.id!!))
    }

    @Test
    internal fun `deve retornar status NOT FOUND se o cliente nao for encontrado durante a delecao`() {
        val chave = geraChavePix()
        repository.save(chave)

        Mockito.`when`(erpClient.buscarClientePorId("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(null)

        assertThrows<StatusRuntimeException> {
            grpcClient.deletarChave(DeletarChavePixGrpcRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890").setIdPix(chave.id.toString()).build())

        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("O cliente não foi encontrado", status.description)
        }
    }

    @Test
    internal fun `deve lancar um status PERMISSION DENIED quando tentar deletar uma chave pix que nao pertence ao cpf`() {
        val chave = geraChavePix()
        repository.save(chave)

        Mockito.`when`(erpClient.buscarClientePorId("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(DadosClienteResponseClient("c56dfef4-7901-44fb-84e2-a2cefb157890","Hugo", "09999999",
                InstituicaoResponse("Itau","929292")
            ))

        assertThrows<StatusRuntimeException> {
            grpcClient.deletarChave(DeletarChavePixGrpcRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890").setIdPix(chave.id.toString()).build())
        }. run {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Você não tem permissão para deletar essa chave PIX", status.description)
        }
    }

    private fun geraChavePix() = ChavePix(
        clienteId = UUID.randomUUID(),
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