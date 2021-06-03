package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.externo.bcb.*
import br.com.zup.hugovallada.pix.ChavePix
import br.com.zup.hugovallada.pix.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaPixEndpointTest(
    @Inject private val repository: ChavePixRepository,
    @Inject private val grpcClient: SearchKeyServiceGrpc.SearchKeyServiceBlockingStub
){

    @Inject
    lateinit var bcbClient: BCBClient

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

    @Test
    internal fun `deve retornar um status NOT FOUND quando nao encontrar no BCB Client`(){
        val chavePix = geraChavePix()
        repository.save(chavePix)
        val request = DadosDeConsultaGrpcInternoRequest.newBuilder()
            .setIdCliente(chavePix.clienteId.toString())
            .setIdPix(chavePix.id.toString()).build()

        Mockito.`when`(bcbClient.buscarChave(UUID.randomUUID().toString()))
            .thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave pix não foi encontra no banco central.", status.description)
        }
    }

    @Test
    internal fun `deve retornar os dados da chave com o id interno e do cliente quando a requisicao vier de um sistema interno`() {
        val chavePix = geraChavePix()
        repository.save(chavePix)
        val request = DadosDeConsultaGrpcInternoRequest.newBuilder()
            .setIdCliente(chavePix.clienteId.toString())
            .setIdPix(chavePix.id.toString()).build()

        Mockito.`when`(bcbClient.buscarChave(chavePix.chave!!))
            .thenReturn(HttpResponse.ok(geraPixDetailResponse()))

        val response = grpcClient.consultarChave(request)

        assertNotNull(response)
        assertTrue(chavePix.clienteId.toString() == response.idCliente)
        assertTrue("email@email.com" == response.chave)
    }

    @Test
    internal fun `deve retornar o status invalid argument quando algum argumento nao for valido `(){
        val request = DadosDeConsultaGrpcInternoRequest.newBuilder()
            .setIdCliente("ola")
            .setIdPix("1234").build()

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }. run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar a chave sem id interno e de cliente quando vier de um sistema externo com resposta vindo do BCB`() {
        val request = DadosDeConsultaGrpcExternoRequest.newBuilder()
            .setChavePix(UUID.randomUUID().toString()).build()

        Mockito.`when`(bcbClient.buscarChave(request.chavePix))
            .thenReturn(HttpResponse.ok(geraPixDetailResponse()))

        val response = grpcClient.consultarChaveExterno(request)

        assertTrue(response.idCliente.isNullOrEmpty())
        assertTrue(response.idPix.isNullOrEmpty())
        assertEquals("email@email.com", response.chave)

    }

    @Test
    internal fun `deve retornar a chave sem id interno e de cliente quando vier de um sistema externo com resposta vindo do proprio sistema`() {
        val chavePix = geraChavePix()
        chavePix.criadaEm = LocalDateTime.now() // TODO: Data
        repository.save(chavePix)

        val request = DadosDeConsultaGrpcExternoRequest.newBuilder()
            .setChavePix(chavePix.chave).build()

        val response = grpcClient.consultarChaveExterno(request)

        assertEquals(chavePix.chave, response.chave)
        assertEquals(chavePix.conta.cpfDoTitular, response.cpf)
    }

    @Test
    internal fun `deve retornar um status not found quando nao existir nem internamente e nem no BCB`() {
        val request = DadosDeConsultaGrpcExternoRequest.newBuilder()
            .setChavePix(UUID.randomUUID().toString()).build()

        Mockito.`when`(bcbClient.buscarChave(UUID.randomUUID().toString()))
            .thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChaveExterno(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave pix não foi encontrada no banco central", status.description)
        }
    }



    private fun geraPixDetailResponse(): PixDetailResponse {
        return PixDetailResponse(
            keyType = KeyType.CPF.toString(),
            key =  "email@email.com",
            bankAccount = BankAccount(participant = Conta.ITAU_UNIBANCO_ISPB,branch= "992882", accountNumber = "0001",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                Type.LEGAL_PERSON,
                "Hugo",
                "9998282"
            ),
            LocalDateTime.now()
        )

    }

    @Factory
    class GrpcClient(){
        @Singleton
        fun geraClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): SearchKeyServiceGrpc.SearchKeyServiceBlockingStub? {
            return SearchKeyServiceGrpc.newBlockingStub(channel)
        }

    }

    @MockBean(BCBClient::class)
    fun mockBCBClient(): BCBClient?{
        return Mockito.mock(BCBClient::class.java)
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