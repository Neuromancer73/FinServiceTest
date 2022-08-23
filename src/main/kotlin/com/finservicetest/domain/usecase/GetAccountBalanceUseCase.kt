package com.finservicetest.domain.usecase

import com.finservicetest.domain.model.AccountDetails

interface GetAccountBalanceUseCase {
    suspend operator fun invoke(accountNumber: Long): Result<AccountDetails>
}