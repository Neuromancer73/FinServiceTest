package com.finservicetest.domain.usecase

interface CheckAccountStatusUseCase {
    suspend operator fun invoke(accountNumber: Long): Result<String>
}
