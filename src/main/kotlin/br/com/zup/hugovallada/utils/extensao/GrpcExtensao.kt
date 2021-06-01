package br.com.zup.hugovallada.utils.extensao

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.DeletarChavePixGrpcRequest
import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.pix.CadastraChavePixRequest
import br.com.zup.hugovallada.pix.DeletarChavePixRequest

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