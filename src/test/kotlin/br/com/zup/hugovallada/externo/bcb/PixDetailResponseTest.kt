package br.com.zup.hugovallada.externo.bcb

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.util.*

internal class PixDetailResponseTest {

    @Test
    internal fun `deve gerar uma ChavePix apos a chamada do metodo toModel`() {
        val pixDetailResponse = PixDetailResponse(
            keyType = KeyType.RANDOM.toString(),
            key = UUID.randomUUID().toString(),
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "99282",
                accountNumber = "9992",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                Type.LEGAL_PERSON,
                name = "Hugo",
                taxIdNumber = "99282882"
            ),
            createdAt = LocalDateTime.now()
        )

        val chave = pixDetailResponse.toModel()

        assertTrue(chave.conta.cpfDoTitular == pixDetailResponse.owner.taxIdNumber)
        assertTrue(chave.conta.instituicao == "ITAÚ UNIBANCO S.A.")
    }
}