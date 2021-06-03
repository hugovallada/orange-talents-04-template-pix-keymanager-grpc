package br.com.zup.hugovallada.pix.consulta

import br.com.zup.hugovallada.utils.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class ConsultaChavePixInternoRequest(
    @field:NotBlank @ValidUUID val idPix: String,
    @field:NotBlank @ValidUUID val idCliente: String
)
