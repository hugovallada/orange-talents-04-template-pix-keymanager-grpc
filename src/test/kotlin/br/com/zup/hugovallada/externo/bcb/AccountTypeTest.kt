package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.TipoDeConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AccountTypeTest{


    @Test
    internal fun `deve retornar CACC apos uma conta do tipo corrente ser convertida`() {
        assertEquals(AccountType.CACC, AccountType.converter(TipoDeConta.CONTA_CORRENTE))
    }

    @Test
    internal fun `deve retornar SVGS apos uma conta do tipo poupanca ser convertida`() {
        assertEquals(AccountType.SVGS, AccountType.converter(TipoDeConta.CONTA_POUPANCA))
    }

    @Test
    internal fun `deve lancar uma excecao apos uma conta de tipo desconhecido ser convertida`() {
        assertThrows<IllegalArgumentException>(){
            AccountType.converter(TipoDeConta.DESCONHECIDA)
        }
    }
}