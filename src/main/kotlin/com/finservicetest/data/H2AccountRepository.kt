package com.finservicetest.data

import com.finservicetest.domain.model.AccountDetails
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component


@Component
class AccountRepository(private val entityTemplate: R2dbcEntityTemplate) {

    suspend fun getAccountDetails(accountNumber: Long): AccountDetails? =
        entityTemplate
            .select<AccountDetails>()
            .from("account")
            .matching(matchingQuery(accountNumber))
            .awaitFirstOrNull()

    suspend fun updateAccountBalance(accountDetails: AccountDetails): Boolean =
        entityTemplate
            .update<AccountDetails>()
            .inTable("account")
            .matching(matchingQuery(accountDetails.accountNumber))
            .applyAndAwait(Update.update("balance", accountDetails.balance))
            .let { it == 1 }

    private fun matchingQuery(accountNumber: Long) =
        query(where("account_number").`is`(accountNumber))
}
