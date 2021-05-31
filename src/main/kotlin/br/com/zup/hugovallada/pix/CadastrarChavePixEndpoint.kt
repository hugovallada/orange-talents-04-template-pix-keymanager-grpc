package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.CadastraChavePixGrpcResponse
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class CadastrarChavePixEndpoint: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    @Transactional
    override fun cadastrarChave(
        request: CadastraChavePixGrpcRequest,
        responseObserver: StreamObserver<CadastraChavePixGrpcResponse>
    ) {

    }
}