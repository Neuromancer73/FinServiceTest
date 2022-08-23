package com.finservicetest.domain.data

import com.finservicetest.domain.model.AccountDetails

interface AccountRepository {

    suspend fun getAccountDetails(accountNumber: Long): AccountDetails?
    suspend fun updateAccountBalance(accountDetails: AccountDetails): Boolean
}
