package br.com.zup.hugovallada.utils.extensao

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.pix.CadastraChavePixRequest
import br.com.zup.hugovallada.pix.DeletarChavePixRequest
import br.com.zup.hugovallada.pix.consulta.ConsultaChavePixInternoRequest

fun CadastraChavePixGrpcRequest.toModel(): CadastraChavePixRequest {
    return CadastraChavePixRequest(
        clienteId = idCliente,
        tipo = when(tipoDeChave){
            TipoDeChave.DESCONHECIDO -> null
            else -> tipoDeChave
        },
        chave = valorChave,
        tipoConta = when(tipoDeConta){
            TipoDeConta.DESCONHECIDA -> null
            else -> tipoDeConta
        }
    )
}

fun DeletarChavePixGrpcRequest.toModel(): DeletarChavePixRequest{
    return DeletarChavePixRequest(
        idCliente = idCliente,
        idPix = idPix
    )
}

fun DadosDeConsultaGrpcInternoRequest.toModel():ConsultaChavePixInternoRequest{
    return ConsultaChavePixInternoRequest(idPix = idPix, idCliente = idCliente)
}