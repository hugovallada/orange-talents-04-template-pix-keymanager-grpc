package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.externo.bcb.AccountType
import br.com.zup.hugovallada.externo.bcb.BCBClient
import br.com.zup.hugovallada.externo.bcb.KeyType
import br.com.zup.hugovallada.pix.ChavePixRepository
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import br.com.zup.hugovallada.utils.excecao.PermissionDeniedException
import br.com.zup.hugovallada.utils.excecao.PixKeyNotFoundException
import br.com.zup.hugovallada.utils.extensao.toModel
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@ErrorHandler
class ConsultaPixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BCBClient
) : SearchKeyServiceGrpc.SearchKeyServiceImplBase() {


    override fun consultarChave(
        request: DadosDeConsultaGrpcInternoRequest,
        responseObserver: StreamObserver<DadosChaveGrpcResponse>
    ) {

        val consultaRequest = isValid(request = request.toModel())

        repository.existsById(UUID.fromString(consultaRequest.idPix))
            .let {
                pixExiste ->
                if(!pixExiste){
                    throw PixKeyNotFoundException("A chave pix de id ${consultaRequest.idPix} não foi encontrada")
                }
            }

        repository.existsByIdAndClienteId(UUID.fromString(consultaRequest.idPix), UUID.fromString(consultaRequest.idCliente))
            .let {
                pixExiste ->
                if(!pixExiste) {
                    throw PermissionDeniedException("A chave que você está tentando consultar, não existe ou não lhe pertence")
                }
            }

        val chave = repository.findById(UUID.fromString(consultaRequest.idPix)).get()

        val responseClient = bcbClient.buscarChave(chave.chave!!)

        //if(responseClient.body() == null) throw PixKeyNotFoundException("Chave Pix não encontrada")

        val resposta = responseClient.body()!!
        responseObserver.onNext(
            DadosChaveGrpcResponse.newBuilder()
                .setIdCliente(chave.clienteId.toString())
                .setIdPix(chave.id.toString())
                .setChave(resposta.key)
                .setTipoDeChave(KeyType.toTipoChave(KeyType.valueOf(resposta.keyType)))
                .setCpf(resposta.owner.taxIdNumber)
                .setTitular(resposta.owner.name)
                .setInstituicao(resposta.bankAccount.participant)
                .setAgencia(resposta.bankAccount.branch)
                .setNumeroDaConta(resposta.bankAccount.accountNumber)
                .setTipoDeConta(AccountType.toTipoConta(resposta.bankAccount.accountType))
                .setDataCriacao(protobufData(resposta.createdAt)).build()

        )
        responseObserver.onCompleted()
    }

    private fun protobufData(data: LocalDateTime): Timestamp? {
        val instant = data.atZone(ZoneId.of("UTC")).toInstant()
        val protoData = Timestamp.newBuilder()
            .setNanos(instant.nano)
            .setSeconds(instant.epochSecond)
            .build()
        return protoData
    }


    @Validated
    private fun isValid(@Valid request: ConsultaChavePixInternoRequest): ConsultaChavePixInternoRequest = request

}