package br.com.zup.hugovallada.pix.consulta.consultando

import br.com.zup.hugovallada.DadosChavePixGrpcResponse
import br.com.zup.hugovallada.DadosDeConsultaGrpcRequest
import br.com.zup.hugovallada.SearchPixKeyServiceGrpc
import br.com.zup.hugovallada.externo.bcb.BCBClient
import br.com.zup.hugovallada.pix.ChavePixRepository
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import br.com.zup.hugovallada.utils.extensao.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaPixKeyEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BCBClient,
    @Inject private val validator: Validator
): SearchPixKeyServiceGrpc.SearchPixKeyServiceImplBase() {

    override fun consultarChave(
        request: DadosDeConsultaGrpcRequest,
        responseObserver: StreamObserver<DadosChavePixGrpcResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(ResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }

}