package br.com.zup.hugovallada.pix.listagem

import br.com.zup.hugovallada.IdDoClienteGrpcRequest
import br.com.zup.hugovallada.ListPixKeyServiceGrpc
import br.com.zup.hugovallada.ListaPixGrpcResponse
import br.com.zup.hugovallada.pix.ChavePixRepository
import br.com.zup.hugovallada.utils.excecao.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListagemPixEndpoint(@Inject private val repository: ChavePixRepository) :
    ListPixKeyServiceGrpc.ListPixKeyServiceImplBase() {

    override fun listarChaves(
        request: IdDoClienteGrpcRequest,
        responseObserver: StreamObserver<ListaPixGrpcResponse>
    ) {
        if (!validar(request.id)) throw IllegalArgumentException("O id é nulo ou não é uma UUID")

        val chaves = repository.findAllByClienteId(UUID.fromString(request.id)).map {
                chavePix ->
            ListaPixGrpcResponse.ChavePixResponse.newBuilder()
                .setPixId(chavePix.id.toString())
                .setClienteId(chavePix.clienteId.toString())
                .setTipo(chavePix.tipo)
                .setTipoConta(chavePix.tipoConta)
                .setValor(chavePix.chave)
                .setCriadaEm(dateToTimestampProto(chavePix.criadaEm!!))
                .build()
        }

        responseObserver.onNext(
            ListaPixGrpcResponse.newBuilder()
                .addAllChaves(chaves)
                .build())
        responseObserver.onCompleted()
    }


    private fun dateToTimestampProto(data: LocalDateTime): Timestamp {
        val timestamp = data.atZone(ZoneId.of("UTC"))

        return Timestamp.newBuilder()
            .setNanos(timestamp.nano)
            .setSeconds(timestamp.toEpochSecond())
            .build()
    }

    private fun validar(idCliente: String): Boolean {
        return idCliente.isNotEmpty() && idCliente.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".toRegex())
    }
}