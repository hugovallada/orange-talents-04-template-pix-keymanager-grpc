package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.DadosChaveGrpcResponse
import br.com.zup.hugovallada.DadosDeConsultaGrpcInternoRequest
import br.com.zup.hugovallada.SearchKeyServiceGrpc
import br.com.zup.hugovallada.pix.ChavePixRepository
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import br.com.zup.hugovallada.utils.excecao.PermissionDeniedException
import br.com.zup.hugovallada.utils.excecao.PixKeyNotFoundException
import br.com.zup.hugovallada.utils.extensao.toModel
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@ErrorHandler
class ConsultaPixEndpoint(@Inject private val repository: ChavePixRepository) : SearchKeyServiceGrpc.SearchKeyServiceImplBase() {


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





    }


    @Validated
    private fun isValid(@Valid request: ConsultaChavePixInternoRequest): ConsultaChavePixInternoRequest = request

}