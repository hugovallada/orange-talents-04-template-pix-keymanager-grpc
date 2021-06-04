package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.DadosDeConsultaGrpcRequest
import br.com.zup.hugovallada.SearchPixKeyServiceGrpc
import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaPixKeyEndpointTest(
    @Inject private val repository: ChavePixRepository,
    @Inject private val grpcClient: SearchPixKeyServiceGrpc.SearchPixKeyServiceBlockingStub
){
    @Inject
    lateinit var bcbClient: BCBClient

    @Test
    internal fun `deve retornar os dados da chave com id da pix e do cliente quando a request tiver esses dados`() {
        val chave = geraChavePix()
        chave.criadaEm = LocalDateTime.now()
        repository.save(chave)

        val request = DadosDeConsultaGrpcRequest.newBuilder()
            .setPixId(DadosDeConsultaGrpcRequest.DadosPorPixId.newBuilder()
                .setClienteId(chave.clienteId.toString()).setPixId(chave.id.toString())).build()


        val response = grpcClient.consultarChave(request)

        assertNotNull(response.chavePix)
    }

    // TODO: Coverage não está contando essa validação
    @ParameterizedTest
    @CsvSource(value = [
        "'',''","email@email, email@email",
    "'',9646d136-0e1a-41e0-b28a-c2e45344bb25"
    ])
    internal fun `deve retornar o status ARGUMENT INVALID quando tentar passar dados da PixId invalidos`(pixId: String, clienteId: String) {
        val request = DadosDeConsultaGrpcRequest.newBuilder().setPixId(
            DadosDeConsultaGrpcRequest.DadosPorPixId.newBuilder()
                .setPixId(pixId)
                .setClienteId(clienteId)
        ).build()
        assertThrows<StatusRuntimeException>{
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar o status NOT FOUND quando nao encontrar o pix id`() {
        val request = DadosDeConsultaGrpcRequest.newBuilder()
            .setPixId(DadosDeConsultaGrpcRequest.DadosPorPixId.newBuilder()
                .setClienteId(UUID.randomUUID().toString()).setPixId(UUID.randomUUID().toString())).build()

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix Id ou client Id incorretos", status.description)
        }
    }

    @Test
    internal fun `deve retornar o status NOT FOUND quando o pix id nao pertencer ao cliente`() {
        val chave = geraChavePix()
        chave.criadaEm = LocalDateTime.now()
        repository.save(chave)

        val request = DadosDeConsultaGrpcRequest.newBuilder()
            .setPixId(DadosDeConsultaGrpcRequest.DadosPorPixId.newBuilder()
                .setClienteId(UUID.randomUUID().toString()).setPixId(chave.id.toString())).build()

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Pix Id ou client Id incorretos", status.description)
        }

    }

    @Test
    internal fun `deve retornar o status FAILED PRECONDITION quando o tipo de filtro nao for encontrado`() {
        val request = DadosDeConsultaGrpcRequest.newBuilder().build()
        assertThrows<StatusRuntimeException>{
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar os dados da pix quando entrar com a chave e ela existir no banco, mas o id da chave e do cliente devem ser nulos`() {
        val chave = geraChavePix()
        chave.criadaEm = LocalDateTime.now()
        repository.save(chave)

        val request = DadosDeConsultaGrpcRequest.newBuilder()
            .setChave(chave.chave).build()

        val response = grpcClient.consultarChave(request)

        assertNotNull(response.chavePix)
        assertTrue(response.idCliente.isNullOrBlank())
        assertTrue(response.idPix.isNullOrBlank())
    }

    @Test
    internal fun `deve retornar o status NOT FOUND quando a chave nao existir no sistema e nem no BCB`() {
        val request = DadosDeConsultaGrpcRequest.newBuilder()
            .setChave(UUID.randomUUID().toString()).build()

        Mockito.`when`(bcbClient.buscarChave(request.chave))
            .thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave pix nao foi encontrada", status.description)
        }

    }

    @Test
    internal fun `deve retornar uma chave sem id do pix e sem id do cliente quando ela for chamada a partir do valor e vir do BCB`() {
        val request = DadosDeConsultaGrpcRequest.newBuilder()
            .setChave("email@email.com").build()

        Mockito.`when`(bcbClient.buscarChave(request.chave))
            .thenReturn(HttpResponse.ok(geraPixDetailResponse()))

        val response = grpcClient.consultarChave(request)
        assertTrue(response.chavePix.chave =="email@email.com")
    }

    @Test
    internal fun `deve retornar status INVALID ARGUMENT caso o valor da chave seja invalido`() {
        val request = DadosDeConsultaGrpcRequest.newBuilder().setChave("").build()

        assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    private fun geraPixDetailResponse(): PixDetailResponse{
        return PixDetailResponse(
            keyType = KeyType.EMAIL.toString(),
            key = "email@email.com",
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "99282",
                accountNumber = "9992",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                Type.LEGAL_PERSON,
                name = "Hugo",
                taxIdNumber = "99282882"
            ),
            createdAt = LocalDateTime.now()
        )
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


    @MockBean(BCBClient::class)
    fun mockBCBClient(): BCBClient? {
        return Mockito.mock(BCBClient::class.java)
    }


    @Factory
    class GrpcClient(){
        @Singleton
        fun gerarClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): SearchPixKeyServiceGrpc.SearchPixKeyServiceBlockingStub? {
            return SearchPixKeyServiceGrpc.newBlockingStub(channel)
        }
    }
}