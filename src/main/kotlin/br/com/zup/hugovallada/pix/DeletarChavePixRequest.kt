package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.utils.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class DeletarChavePixRequest(
    @field:NotBlank @ValidUUID
    val idPix: String,
    @field:NotBlank @ValidUUID
    val idCliente: String,
)
