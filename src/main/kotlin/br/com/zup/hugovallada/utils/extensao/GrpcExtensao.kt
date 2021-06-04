package br.com.zup.hugovallada.utils.extensao

import br.com.zup.hugovallada.*
import br.com.zup.hugovallada.DadosDeConsultaGrpcRequest.FiltroCase.*
import br.com.zup.hugovallada.TipoDeConta.CONTA_CORRENTE
import br.com.zup.hugovallada.TipoDeConta.CONTA_POUPANCA
import br.com.zup.hugovallada.externo.bcb.AccountType
import br.com.zup.hugovallada.externo.bcb.AccountType.CACC
import br.com.zup.hugovallada.externo.bcb.AccountType.SVGS
import br.com.zup.hugovallada.pix.CadastraChavePixRequest
import br.com.zup.hugovallada.pix.DeletarChavePixRequest
import br.com.zup.hugovallada.pix.consulta.ConsultaChavePixInternoRequest
import br.com.zup.hugovallada.pix.consulta.ConsultaChavePixRequest
import br.com.zup.hugovallada.pix.consulta.consultando.Filtro
import javax.validation.ConstraintViolationException
import javax.validation.Validator

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

fun DadosDeConsultaGrpcExternoRequest.toModel():ConsultaChavePixRequest{
    return ConsultaChavePixRequest(chavePix= chavePix)
}

fun DadosDeConsultaGrpcRequest.toModel(validator: Validator): Filtro {
    val filtro = when(filtroCase){
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido
    }

    val violations = validator.validate(filtro)
    if(violations.isNotEmpty()){
        throw ConstraintViolationException(violations)
    }

    return filtro
}