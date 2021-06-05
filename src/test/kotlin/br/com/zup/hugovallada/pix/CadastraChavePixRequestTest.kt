package br.com.zup.hugovallada.pix

import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.TipoDeConta
import br.com.zup.hugovallada.conta.Conta
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import java.util.*

internal class CadastraChavePixRequestTest {

    lateinit var conta: Conta

    @BeforeEach
    internal fun setUp() {
        conta = Conta(
            instituicao = "Itau",
            nomeDoTitular = "Hugo",
            cpfDoTitular = "827228191",
            agencia = "8272",
            numeroDaConta = "0001"
        )
    }

    @Test
    fun `deve retornar uma chave pix com chave nula quando o tipo for aleatorio`() {
        val chavePixRequest = CadastraChavePixRequest(
            clienteId = UUID.randomUUID().toString(),
            tipoConta = TipoDeConta.CONTA_POUPANCA,
            tipo = TipoDeChave.CHAVE_ALEATORIA,
            chave = ""
        )

        val chave = chavePixRequest.toModel(conta = conta)

        assertNull(chave.chave)
    }


    @ParameterizedTest
    @CsvSource(value = [
        "EMAIL, email@email","TELEFONE_CELULAR, +551189829282","CPF, 40530913062"
    ])
    internal fun `deve retornar uma chave pix com uma chave com valor quando o tipo nao for aleatorio`(tipo: String, chave: String) {
        val chavePixRequest = CadastraChavePixRequest(
            clienteId = UUID.randomUUID().toString(),
            tipo = TipoDeChave.valueOf(tipo),
            tipoConta = TipoDeConta.CONTA_CORRENTE,
            chave = chave
        )

        val chaveGerada = chavePixRequest.toModel(conta)
        assertNotNull(chaveGerada.chave)
    }
}