package com.finservicetest.domain.service

import com.finservicetest.domain.data.AccountRepository
import com.finservicetest.domain.usecase.GetAccountBalanceUseCase
import org.springframework.stereotype.Service

@Service
class GetAccountBalance(val accountRepository: AccountRepository) : GetAccountBalanceUseCase {

    override suspend fun invoke(accountNumber: Long) =
        accountRepository.getAccountDetails(accountNumber)
            ?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException())
}
