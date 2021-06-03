package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.conta.Conta

data class DeletePixKeyRequest(
    val participant: String = Conta.ITAU_UNIBANCO_ISPB,
    val key: String?
)
