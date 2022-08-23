package com.finservicetest.domain.model

import java.math.BigDecimal

data class AccountDetails(
    val accountNumber: Long,
    val currency: Int,
    val balance: BigDecimal,
    val status: AccountStatus
)

enum class AccountStatus {
    OPEN, CLOSED
}
