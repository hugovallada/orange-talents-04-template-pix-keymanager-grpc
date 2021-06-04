package br.com.zup.hugovallada.pix.consulta.consultando

import br.com.zup.hugovallada.DadosChavePixGrpcResponse
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.externo.bcb.AccountType
import br.com.zup.hugovallada.externo.bcb.BankAccount
import br.com.zup.hugovallada.externo.bcb.KeyType
import br.com.zup.hugovallada.externo.bcb.Owner
import java.time.LocalDateTime

data class DadosPixKeyResponse (
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
        ) {

    fun toModel(): DadosDaPix{
        return DadosDaPix(
            tipo = KeyType.toTipoChave(keyType),
            chave = key,
            tipoDeConta = AccountType.toTipoConta(bankAccount.accountType),
            conta = Conta(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}