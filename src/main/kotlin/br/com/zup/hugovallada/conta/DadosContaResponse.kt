package br.com.zup.hugovallada.conta

import javax.print.DocFlavor

class DadosContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)

class InstituicaoResponse(val nome: String, val ispb: String)

class TitularResponse(val id: String, val nome: String, val cpf: String)
