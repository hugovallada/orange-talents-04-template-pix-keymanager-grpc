package br.com.zup.hugovallada.utils.extensao

import br.com.zup.hugovallada.CadastraChavePixGrpcRequest
import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.pix.CadastraChavePixRequest

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