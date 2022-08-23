package com.finservicetest.domain.usecases

import com.finservicetest.domain.model.AccountDetails

interface GetAccountBalanceUseCase {
    suspend operator fun invoke(accountNumber: String?): AccountDetails
}