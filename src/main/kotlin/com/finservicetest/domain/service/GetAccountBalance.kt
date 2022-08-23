package com.finservicetest.domain.service

import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.usecase.GetAccountBalanceUseCase

class GetAccountBalanceService : GetAccountBalanceUseCase {
    override suspend fun invoke(accountNumber: String?): AccountDetails {
        TODO("Not yet implemented")
    }
}