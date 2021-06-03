package br.com.zup.hugovallada.externo.bcb

import java.time.LocalDateTime

data class PixDetailResponse(
    val keyType:String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)