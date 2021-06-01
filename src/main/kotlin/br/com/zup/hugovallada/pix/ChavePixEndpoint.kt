package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.externo.ItauERPClient
import br.com.zup.hugovallada.utils.excecao.ClientNotFoundException
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import br.com.zup.hugovallada.utils.excecao.ExistingPixKeyException
import br.com.zup.hugovallada.utils.excecao.PixKeyNotFoundException
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
    @Inject private val erpClient: ItauERPClient
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
        repository.save(chave)

        responseObserver.onNext(
            CadastraChavePixGrpcResponse.newBuilder()
                .setId(chave.id.toString()).build()
        )
        responseObserver.onCompleted()

    }

    override fun deletarChave(
        request: DeletarChavePixGrpcRequest,
        responseObserver: StreamObserver<DeletarChavePixGrpcResponse>
    ) {
        val chaveASerDeletada = validar(request = request.toModel())

        if(!repository.existsById(UUID.fromString(chaveASerDeletada.idPix))){
            throw PixKeyNotFoundException("A chave Pix não foi encontrada")
        }

        val response = erpClient.buscarClientePorId(chaveASerDeletada.idCliente)

        responseObserver.onNext(DeletarChavePixGrpcResponse.newBuilder().setSucesso(true).build())
        responseObserver.onCompleted()
    }


    @Validated
    fun validar(@Valid request: CadastraChavePixRequest): CadastraChavePixRequest {
        return request
    }
    @Validated
    private fun validar(@Valid request: DeletarChavePixRequest) = request
}