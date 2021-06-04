package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.pix.consulta.DadosDaPix
import br.com.zup.hugovallada.pix.consulta.Instituicoes
import java.time.LocalDateTime

data class PixDetailResponse(
    val keyType:String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
){
    fun toModel() : DadosDaPix {
        return DadosDaPix(
            tipo = KeyType.toTipoChave(keyType = KeyType.valueOf(keyType)),
            chave = key,
            tipoDeConta = AccountType.toTipoConta(bankAccount.accountType),
            conta = Conta(
                instituicao = Instituicoes.nome(participant = bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}
