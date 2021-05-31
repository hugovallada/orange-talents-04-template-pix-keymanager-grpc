package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.conta.Conta
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    val clienteId: UUID,
    @Enumerated(EnumType.STRING) @field:NotNull
    val tipo: TipoDeChave,
    @field:NotBlank
    val chave: String,
    @Enumerated(EnumType.STRING) @field:NotNull
    val tipoConta: TipoDeConta,
    @ManyToOne(cascade = [CascadeType.MERGE])
    val conta: Conta
) {
    @Id @GeneratedValue
    var id: UUID? = null
}