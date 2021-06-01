package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.DeletarChavePixGrpcRequest
import br.com.zup.hugovallada.DeletarChavePixGrpcResponse
import br.com.zup.hugovallada.KeyManagerGrpcServiceGrpc
import br.com.zup.hugovallada.externo.ItauERPClient
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import br.com.zup.hugovallada.utils.excecao.PixKeyNotFoundException
import br.com.zup.hugovallada.utils.extensao.toModel
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@ErrorHandler
@Singleton
class DeletarChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val erpClient: ItauERPClient
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {
    override fun deletarChave(request: DeletarChavePixGrpcRequest, responseObserver: StreamObserver<DeletarChavePixGrpcResponse>) {
        val chaveASerDeletada = validar(request = request.toModel())

        if(!repository.existsById(UUID.fromString(chaveASerDeletada.idPix))){
            throw PixKeyNotFoundException("A chave Pix não foi encontrada")
        }

        val response = erpClient.buscarClientePorId(chaveASerDeletada.idCliente)

        responseObserver.onNext(DeletarChavePixGrpcResponse.newBuilder().setSucesso(true).build())
        responseObserver.onCompleted()


    }


    @Validated
    private fun validar(@Valid request: DeletarChavePixRequest) = request


}