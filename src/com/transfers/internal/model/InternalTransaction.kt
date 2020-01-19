package com.transfers.internal.model

import java.math.BigDecimal
import java.util.UUID

data class InternalTransaction(
    val receiverAccountId: UUID,
    val senderAccountId: UUID,
    val amount: BigDecimal
)