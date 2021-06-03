package br.com.zup.hugovallada.pix.consulta

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
class ConsultaChavePixRequest(
    @field:NotBlank @field:Size(max = 77, message = "O tamanho não deve exceder 77 caracteres")
    val chavePix: String
)
