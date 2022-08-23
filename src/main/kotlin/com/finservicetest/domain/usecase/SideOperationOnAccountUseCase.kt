package com.finservicetest.domain.usecase

import com.finservicetest.domain.model.AccountOperation

interface SideOperationOnAccountUseCase {
    suspend operator fun invoke(accountOperation: AccountOperation): Result<Unit>
}
