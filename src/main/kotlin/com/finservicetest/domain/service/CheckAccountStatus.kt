package com.finservicetest.domain.service

import com.finservicetest.data.AccountRepository
import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.usecase.GetAccountBalanceUseCase
import com.finservicetest.domain.util.toAccountDetails
import org.springframework.stereotype.Component

@Component
class GetAccountStatus(val accountRepository: AccountRepository) : GetAccountBalanceUseCase {

    override suspend fun invoke(accountNumber: String?): Result<AccountDetails> {
        if (accountNumber.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException())
        }
        val parsedAccountNumber =
            try {
                accountNumber.toLong()
            } catch (exception: NumberFormatException) {
                return Result.failure(IllegalArgumentException())
            }
        accountRepository.findById(parsedAccountNumber)?.let {
                return Result.success(it.toAccountDetails())
            }
        return Result.failure(NoSuchElementException())
    }
}
