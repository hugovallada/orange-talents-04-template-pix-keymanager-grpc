package br.com.zup.hugovallada.externo.itau

import br.com.zup.hugovallada.conta.InstituicaoResponse

data class DadosClienteResponseClient(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
)
