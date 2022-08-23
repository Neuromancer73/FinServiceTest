package com.finservicetest.domain.model

import java.math.BigDecimal

data class AccountOperation(
    val accountNumber: Long,
    val currency: Int,
    val amount: BigDecimal,
    val accountOperationType: AccountOperationType
)

enum class AccountOperationType {
    DEBIT, CREDIT
}
