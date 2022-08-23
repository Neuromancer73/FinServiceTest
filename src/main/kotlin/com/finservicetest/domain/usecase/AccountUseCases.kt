package com.finservicetest.domain.usecase

import org.springframework.stereotype.Component

@Component
data class AccountUseCases(
    val checkAccountStatusUseCase: CheckAccountStatusUseCase,
    val getAccountBalanceUseCase: GetAccountBalanceUseCase,
    val sideOperationOnAccountUseCase: SideOperationOnAccountUseCase
)
