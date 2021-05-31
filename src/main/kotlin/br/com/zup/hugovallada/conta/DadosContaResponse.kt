package br.com.zup.hugovallada.conta

data class DadosContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
){
    fun toModel(): Conta {
        return Conta(
            instituicao = this.instituicao.nome,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = this.numero
        )
    }
}

data class InstituicaoResponse(val nome: String, val ispb: String)

data class TitularResponse(val nome: String, val cpf: String)
