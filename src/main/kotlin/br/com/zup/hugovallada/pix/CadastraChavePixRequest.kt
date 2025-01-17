package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.conta.Conta
import br.com.zup.hugovallada.utils.validacao.ValidPixKey
import br.com.zup.hugovallada.utils.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidPixKey
data class CadastraChavePixRequest(
    @field:NotBlank @ValidUUID
    val clienteId: String,
    @field:NotNull
    val tipo: TipoDeChave?,
    @field:Size(max=77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoDeConta?
){
    fun toModel(conta: Conta) : ChavePix{
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipo = TipoDeChave.valueOf(this.tipo!!.name),
            chave = if(this.tipo == TipoDeChave.CHAVE_ALEATORIA) null else this.chave!!,
            tipoConta = TipoDeConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }
}
