package com.transfers.internal.model

import java.math.BigDecimal
import java.util.UUID

data class Account(val id: UUID, var balance: BigDecimal)