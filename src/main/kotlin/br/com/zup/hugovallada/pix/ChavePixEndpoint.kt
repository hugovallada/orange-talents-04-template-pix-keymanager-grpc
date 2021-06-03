package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.externo.bcb.BCBClient
import br.com.zup.hugovallada.externo.bcb.CreatePixKeyRequest
import br.com.zup.hugovallada.externo.bcb.DeletePixKeyRequest
import br.com.zup.hugovallada.externo.itau.ItauERPClient
import br.com.zup.hugovallada.utils.excecao.*
import br.com.zup.hugovallada.utils.extensao.toModel
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@ErrorHandler
@Singleton
class ChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val erpClient: ItauERPClient,
    @Inject private val bcbClient: BCBClient
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    @Transactional
    override fun cadastrarChave(
        request: CadastraChavePixGrpcRequest,
        responseObserver: StreamObserver<CadastraChavePixGrpcResponse>
    ) {
        val novaChave = validar(request = request.toModel())

        if (repository.existsByChave(novaChave.chave!!)) {
            throw ExistingPixKeyException("Essa chave já está cadastrada")
        }

        val response = erpClient.buscarClientePorConta(novaChave.clienteId, novaChave.tipoConta!!.name)
            ?: throw ClientNotFoundException("O cliente não foi encontrado")

        val chave = novaChave.toModel(response.toModel())


        val pixBCB = bcbClient.criarChave(CreatePixKeyRequest(chave, response))

        if (chave.tipo == TipoDeChave.CHAVE_ALEATORIA) {

            chave.chave = pixBCB.body()!!.key
        }

        val novaChavePix = repository.save(chave)

        responseObserver.onNext(
            CadastraChavePixGrpcResponse.newBuilder()
                .setId(novaChavePix.id.toString()).build()
        )
        responseObserver.onCompleted()
    }

    @Transactional
    override fun deletarChave(
        request: DeletarChavePixGrpcRequest,
        responseObserver: StreamObserver<DeletarChavePixGrpcResponse>
    ) {
        val chaveASerDeletada = validar(request = request.toModel())

        if (!repository.existsById(UUID.fromString(chaveASerDeletada.idPix))) {
            throw PixKeyNotFoundException("A chave Pix não foi encontrada")
        }

        val response = erpClient.buscarClientePorId(chaveASerDeletada.idCliente)
            ?: throw ClientNotFoundException("O cliente não foi encontrado")

        if (!repository.existsByIdAndContaCpfDoTitular(UUID.fromString(chaveASerDeletada.idPix), response.cpf)) {
            throw PermissionDeniedException("Você não tem permissão para deletar essa chave PIX")
        }

        val chave = repository.findById(UUID.fromString(chaveASerDeletada.idPix)).get()


        val responseDel = bcbClient.deletarChave(
            DeletePixKeyRequest(key = chave.chave!!),
            chave.chave!!
        )

        repository.deleteById(UUID.fromString(chaveASerDeletada.idPix))

        responseObserver.onNext(
            DeletarChavePixGrpcResponse.newBuilder().setMensagem("Chave Pix ${responseDel.body()!!.key} deletada com sucesso").build()
        )
        responseObserver.onCompleted()
    }


    @Validated
    fun validar(@Valid request: CadastraChavePixRequest) = request

    @Validated
    private fun validar(@Valid request: DeletarChavePixRequest) = request
}