package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.CadastraChavePixGrpcResponse
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import br.com.zup.hugovallada.utils.extensao.toModel
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class CadastrarChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    @Transactional
    override fun cadastrarChave(
        request: CadastraChavePixGrpcRequest,
        responseObserver: StreamObserver<CadastraChavePixGrpcResponse>
    ) {

        val novaChave = request.toModel()

        if(repository.existsByChave(novaChave.chave!!)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Essa chave já está cadastrada")
                .asRuntimeException())
        }



    }
}