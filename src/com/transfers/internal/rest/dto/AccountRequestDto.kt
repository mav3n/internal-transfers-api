package com.transfers.internal.rest.dto

import java.math.BigDecimal
import javax.validation.constraints.NotNull

data class AccountRequestDto(
    @field:NotNull
    val balance: BigDecimal?
)