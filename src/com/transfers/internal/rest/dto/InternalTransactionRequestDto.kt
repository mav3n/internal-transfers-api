package com.transfers.internal.rest.dto

import com.transfers.internal.model.InternalTransaction
import java.math.BigDecimal
import java.util.UUID
import javax.validation.constraints.NotNull

data class InternalTransactionRequestDto(
    @field:NotNull
    val receiverAccountId: UUID?,
    @field:NotNull
    val senderAccountId: UUID?,
    @field:NotNull
    val amount: BigDecimal?
)

fun InternalTransactionRequestDto.toModel() =
    InternalTransaction(
        requireNotNull(this.receiverAccountId),
        requireNotNull(this.senderAccountId),
        requireNotNull(this.amount)
    )