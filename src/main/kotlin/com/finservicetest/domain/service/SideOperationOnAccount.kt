package com.finservicetest.domain.service

import com.finservicetest.domain.data.AccountRepository
import com.finservicetest.domain.model.AccountOperation
import com.finservicetest.domain.model.AccountOperationType.CREDIT
import com.finservicetest.domain.model.AccountOperationType.DEBIT
import com.finservicetest.domain.model.AccountStatus.OPEN
import com.finservicetest.domain.usecase.SideOperationOnAccountUseCase
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.math.BigDecimal

@Service
class SideOperationOnAccount(
    private val repository: AccountRepository,
    transactionManager: ReactiveTransactionManager
) : SideOperationOnAccountUseCase {

    private val definition = DefaultTransactionDefinition().apply {
        isolationLevel = TransactionDefinition.ISOLATION_READ_UNCOMMITTED
        timeout = 1
    }
    private val transactionalOperator = TransactionalOperator.create(transactionManager, definition)

    override suspend fun invoke(accountOperation: AccountOperation) =
        transactionalOperator
            .executeAndAwait { accountOperation.choseOperationPerformer().perform() }
            ?: Result.failure(Exception())

    private fun AccountOperation.choseOperationPerformer() =
        when (accountOperationType) {
            DEBIT -> DebitOperationPerformer(this, repository)
            CREDIT -> CreditOperationPerformer(this, repository)
        }

    private interface OperationPerformer {
        val accountOperation: AccountOperation
        suspend fun perform(): Result<Unit>
    }

    private class DebitOperationPerformer(
        override val accountOperation: AccountOperation,
        val repository: AccountRepository
    ) : OperationPerformer {
        override suspend fun perform() =
            coroutineScope {
                val accountDetails = repository.getAccountDetails(accountOperation.accountNumber)
                    ?: return@coroutineScope Result.failure(Exception())

                val (_, operationCurrency, operationAmount) = accountOperation
                val (_, accountCurrency, accountBalance, accountStatus) = accountDetails
                val newBalance = accountBalance - operationAmount

                when {
                    accountStatus != OPEN -> return@coroutineScope Result.failure(Exception())
                    accountCurrency != operationCurrency -> return@coroutineScope Result.failure(Exception())
                    operationAmount < BigDecimal.ZERO -> return@coroutineScope Result.failure(Exception())
                    newBalance < BigDecimal.ZERO -> return@coroutineScope Result.failure(Exception())
                }

                repository.updateAccountBalance(accountDetails.copy(balance = newBalance))
                    .let { isUpdated ->
                        if (isUpdated) {
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception())
                        }
                    }
            }
    }

    private class CreditOperationPerformer(
        override val accountOperation: AccountOperation,
        val repository: AccountRepository
    ) : OperationPerformer {
        override suspend fun perform() =
            coroutineScope {

                val accountDetails = repository.getAccountDetails(accountOperation.accountNumber)
                    ?: return@coroutineScope Result.failure(Exception())

                val (_, operationCurrency, operationAmount) = accountOperation
                val (_, accountCurrency, accountBalance, accountStatus) = accountDetails
                val newBalance = accountBalance + operationAmount
                when {
                    accountStatus != OPEN -> return@coroutineScope Result.failure(Exception())
                    accountCurrency != operationCurrency -> return@coroutineScope Result.failure(Exception())
                    operationAmount < BigDecimal.ZERO -> return@coroutineScope Result.failure(Exception())
                }

                repository.updateAccountBalance(accountDetails.copy(balance = newBalance))
                    .let { isUpdated ->
                        if (isUpdated) {
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception())
                        }
                    }
            }
    }
}
