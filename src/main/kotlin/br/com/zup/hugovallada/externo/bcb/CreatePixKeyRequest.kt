package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeChave.*
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.TipoDeConta.CONTA_CORRENTE
import br.com.zup.hugovallada.TipoDeConta.CONTA_POUPANCA
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.conta.DadosContaResponse
import br.com.zup.hugovallada.pix.ChavePix
import java.lang.IllegalArgumentException

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String?=null,
    val bankAccount: BankAccount,
    val owner: Owner,
){


    constructor(chavePix: ChavePix, dadosContaResponse: DadosContaResponse) : this(
        keyType = KeyType.converter(chavePix.tipo),
        key = if(chavePix.tipo == CHAVE_ALEATORIA) null else chavePix.chave,
        bankAccount = BankAccount(
            participant = Conta.ITAU_UNIBANCO_ISPB,
            branch = dadosContaResponse.agencia,
            accountNumber = dadosContaResponse.numero,
            accountType = AccountType.converter(tipoDeConta = chavePix.tipoConta)
        ),
        owner = Owner(
            type = Type.LEGAL_PERSON,
            name = dadosContaResponse.titular.nome,
            taxIdNumber = dadosContaResponse.titular.cpf
        )
    )
}

data class Owner(
    val type: Type,
    val name: String,
    val taxIdNumber: String
)

enum class Type{
    NATURAL_PERSON,
    LEGAL_PERSON
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

enum class AccountType{
    CACC,
    SVGS;

    companion object{
        fun converter(tipoDeConta: TipoDeConta): AccountType{
            return when(tipoDeConta){
                CONTA_CORRENTE -> CACC
                CONTA_POUPANCA -> SVGS
                else -> throw IllegalArgumentException("Não existe esse tipo de conta")
            }
        }

        fun toTipoConta(accountType: AccountType?): TipoDeConta{
            return when(accountType){
                CACC -> CONTA_CORRENTE
                SVGS -> CONTA_POUPANCA
                else -> throw IllegalArgumentException("Não existe esse tipo de conta")
            }
        }
    }


}

enum class KeyType(){
    CPF,
    CNPJ,
    RANDOM,
    EMAIL,
    PHONE;

    companion object {
        fun converter(tipoDeChave: TipoDeChave): KeyType {
            return when (tipoDeChave) {
                CHAVE_ALEATORIA -> RANDOM
                TipoDeChave.CPF -> CPF
                TipoDeChave.TELEFONE_CELULAR -> PHONE
                TipoDeChave.EMAIL -> EMAIL
                else -> throw IllegalArgumentException("Chave inválida")
            }
        }

        fun toTipoChave(keyType: KeyType): TipoDeChave{
            return when (keyType) {
                EMAIL -> TipoDeChave.EMAIL
                PHONE -> TELEFONE_CELULAR
                CPF -> TipoDeChave.CPF
                RANDOM -> CHAVE_ALEATORIA
                else -> throw IllegalArgumentException("Chave inválida")
            }
        }
    }

}