package br.com.zup.hugovallada.pix.consulta.consultando

import br.com.zup.hugovallada.externo.bcb.BCBClient
import br.com.zup.hugovallada.pix.ChavePixRepository
import br.com.zup.hugovallada.utils.excecao.PixKeyNotFoundException
import br.com.zup.hugovallada.utils.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {
    abstract  fun filtra(repository: ChavePixRepository, bcbClient: BCBClient):DadosDaPix

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ): Filtro(){
        private fun pixIdAsUuid(): UUID = UUID.fromString(pixId)
        private fun clienteIdAsUuid(): UUID = UUID.fromString(clienteId)

        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): DadosDaPix {
            return repository.findById(pixIdAsUuid())
                .filter{
                    it.pertenceAo(clienteIdAsUuid())
                }.map(DadosDaPix::of)
                .orElseThrow { PixKeyNotFoundException("") }
        }

    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max=77) val chave: String): Filtro(){
        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): DadosDaPix {
            val chave2 = repository.findByChave(chave)
            if(chave2.isPresent){
                val chaveEncontrada = chave2.get()
                val chaveResposta = DadosDaPix.of(chaveEncontrada)
                chaveResposta.clientId = null
                chaveResposta.pixId = null
                return chaveResposta
            }

            val response = bcbClient.buscarChave(chave)
            return when(response.status){
                HttpStatus.OK -> response.body()?.toModel()!!
                else -> throw PixKeyNotFoundException("")
            }
        }
    }

    @Introspected
    object Invalido : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): DadosDaPix {
            TODO("Not yet implemented")
        }
    }
}
