package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.TipoDeChave
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.lang.IllegalArgumentException

internal class KeyTypeTest{


    @ParameterizedTest
    @CsvSource(value = [
        "CHAVE_ALEATORIA, RANDOM","CPF, CPF",
        "TELEFONE_CELULAR, PHONE","EMAIL, EMAIL"
    ])
    internal fun `deve retornar p valor correto apos a conversao`(tipoChave: String, keyType: String) {
        assertEquals(KeyType.valueOf(keyType), KeyType.converter(TipoDeChave.valueOf(tipoChave)))
    }

    @Test
    internal fun `deve lancar uma excecao quando tentar converter um tipo de chave desconhecido` (){
        assertThrows<IllegalArgumentException>{
            KeyType.converter(TipoDeChave.DESCONHECIDO)
        }
    }
}