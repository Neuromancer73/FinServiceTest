package com.finservicetest.domain.usecases

interface CheckAccountStatusUseCase {
    suspend operator fun invoke(accountNumber: String?): String
}
