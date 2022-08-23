package com.finservicetest.domain.service

import com.finservicetest.domain.data.AccountRepository
import com.finservicetest.domain.usecase.CheckAccountStatusUseCase
import org.springframework.stereotype.Service

@Service
class CheckAccountStatus(val accountRepository: AccountRepository) : CheckAccountStatusUseCase {

    override suspend fun invoke(accountNumber: Long) =
        accountRepository.getAccountDetails(accountNumber)
            ?.let { Result.success(it.status.toString()) }
            ?: Result.failure(NoSuchElementException())
}
