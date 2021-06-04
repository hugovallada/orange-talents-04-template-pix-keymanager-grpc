package br.com.zup.hugovallada.pix.consulta.consultando

import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.pix.ChavePix
import java.time.LocalDateTime
import java.util.*

data class DadosDaPix(
    var pixId: UUID? = null,
    var clientId: UUID? = null,
    val tipo: TipoDeChave,
    val chave: String,
    val tipoDeConta: TipoDeConta,
    val conta: Conta,
    val registradaEm: LocalDateTime = LocalDateTime.now()
    ) {

    companion object{
        fun of(chave: ChavePix): DadosDaPix{
            return DadosDaPix(
                pixId = chave.id,
                clientId = chave.clienteId,
                tipo = chave.tipo,
                chave = chave.chave!!,
                tipoDeConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm!!
            )
        }
    }

}
