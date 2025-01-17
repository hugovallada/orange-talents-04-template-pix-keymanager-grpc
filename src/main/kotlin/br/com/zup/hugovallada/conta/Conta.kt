package br.com.zup.hugovallada.conta

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
class Conta(
    @field:NotBlank
    val instituicao: String,
    @field:NotBlank
    val nomeDoTitular: String,
    @field:NotBlank
    val cpfDoTitular: String,
    @field:NotBlank
    val agencia: String,
    @field:NotBlank
    val numeroDaConta: String
) {
    @Id @GeneratedValue
    var id: UUID? = null

    companion object{
        val ITAU_UNIBANCO_ISPB: String = "60701190"
    }

}
