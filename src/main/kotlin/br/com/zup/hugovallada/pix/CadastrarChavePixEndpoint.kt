package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.CadastraChavePixGrpcResponse
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.externo.ItauERPClient
import br.com.zup.hugovallada.utils.extensao.toModel
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
class CadastrarChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val erpClient: ItauERPClient
): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    @Transactional
    override fun cadastrarChave(
        request: CadastraChavePixGrpcRequest,
        responseObserver: StreamObserver<CadastraChavePixGrpcResponse>
    ) {
        val novaChave = validar(request = request.toModel())

        if(repository.existsByChave(novaChave.chave!!)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Essa chave já está cadastrada")
                .asRuntimeException())
            return
        }

        val response = erpClient.buscarClientePorConta(novaChave.clienteId, novaChave.tipoConta!!.name)

        if(response == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("O cliente não foi encontrado")
                .asRuntimeException())
            return
        }

        val chave = novaChave.toModel(response.toModel())
        repository.save(chave)

        responseObserver.onNext(CadastraChavePixGrpcResponse.newBuilder()
            .setId(chave.id.toString()).build())
        responseObserver.onCompleted()

    }

    @Validated
    fun validar(@Valid request: CadastraChavePixRequest): CadastraChavePixRequest {
        return request
    }
}