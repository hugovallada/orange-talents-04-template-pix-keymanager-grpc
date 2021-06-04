package br.com.zup.hugovallada.pix.consulta.consultando

import br.com.zup.hugovallada.DadosChaveGrpcResponse
import br.com.zup.hugovallada.DadosChavePixGrpcResponse
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ResponseConverter {

    fun convert(dadosDaPix: DadosDaPix): DadosChavePixGrpcResponse {
        return DadosChavePixGrpcResponse.newBuilder()
            .setIdCliente(dadosDaPix.clientId?.toString()?: "")
            .setIdPix(dadosDaPix.pixId?.toString() ?: "")
            .setChavePix(
                DadosChavePixGrpcResponse.DadosChavePix.newBuilder()
                    .setTipo(dadosDaPix.tipo)
                    .setChave(dadosDaPix.chave)
                    .setConta(DadosChavePixGrpcResponse.DadosChavePix.DadosConta.newBuilder()
                        .setTipo(dadosDaPix.tipoDeConta)
                        .setInstituicao(dadosDaPix.conta.instituicao)
                        .setNomeDoTitular(dadosDaPix.conta.nomeDoTitular)
                        .setCpfDoTitular(dadosDaPix.conta.cpfDoTitular)
                        .setNumeroDaConta(dadosDaPix.conta.numeroDaConta)
                        .build())
                    .setCriadaEm(dadosDaPix.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }
}