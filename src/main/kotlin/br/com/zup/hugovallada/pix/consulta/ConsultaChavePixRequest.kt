package br.com.zup.hugovallada.pix.consulta

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
class ConsultaChavePixRequest(
    @field:NotBlank @field:Size(max=77) @JsonProperty("chavePix")
    val chavePix: String
)
