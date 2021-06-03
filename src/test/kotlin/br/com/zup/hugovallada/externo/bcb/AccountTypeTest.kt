package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.TipoDeConta
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

    @ParameterizedTest
    @CsvSource(value = [
        "SVGS, CONTA_POUPANCA","CACC, CONTA_CORRENTE"
    ])
    internal fun `deve retornar uma poupanca quando a entrada for SVGS e uma corrente quando a entrada for CACC`(accountType: String, tipoDeConta: String) {

        assertEquals(TipoDeConta.valueOf(tipoDeConta), AccountType.toTipoConta(AccountType.valueOf(accountType)))
    }

    @Test
    internal fun `deve lancar uma excecao apos uma account type de tipo desconhecido tentar ser transformada em tipo conta`() {
        assertThrows<java.lang.IllegalArgumentException> {
            AccountType.toTipoConta(null)
        }.run {
            assertEquals("Não existe esse tipo de conta", message)
        }
    }
}