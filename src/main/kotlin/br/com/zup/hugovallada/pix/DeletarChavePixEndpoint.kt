package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.DeletarChavePixGrpcRequest
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import br.com.zup.hugovallada.externo.ItauERPClient
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletarChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val erpClient: ItauERPClient
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {


    override fun deletarChave(request: DeletarChavePixGrpcRequest, responseObserver: StreamObserver<Empty>) {
    }


}