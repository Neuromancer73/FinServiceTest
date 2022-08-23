package com.finservicetest.domain.usecases

import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.model.AccountOperation

interface SideOperationOnAccountUseCase {
    suspend operator fun invoke(accountOperation: AccountOperation): AccountDetails
}