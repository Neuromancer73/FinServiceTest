package com.finservicetest.data

import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.model.AccountStatus
import com.finservicetest.domain.model.AccountStatus.CLOSED
import com.finservicetest.domain.model.AccountStatus.OPEN
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.insert
import org.springframework.data.r2dbc.dialect.H2Dialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.ReactiveTransactionManager
import java.math.BigDecimal
import java.time.Duration

@Configuration
@EnableR2dbcRepositories
class DatabaseConfig : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory() =
        H2ConnectionFactory.inMemory("finservicetestdb")

    @Bean
    fun reactiveTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    override fun r2dbcEntityTemplate(
        databaseClient: DatabaseClient,
        dataAccessStrategy: ReactiveDataAccessStrategy
    ) =
        R2dbcEntityTemplate(databaseClient, H2Dialect.INSTANCE)
            .apply {
                initTable()
                insertTestData()
            }

    private fun R2dbcEntityTemplate.initTable() {
        databaseClient
            .sql("CREATE TABLE account (account_number LONG PRIMARY KEY, currency INTEGER, balance NUMERIC, status VARCHAR(6) check (status in ('OPEN', 'CLOSED')));")
            .fetch()
            .rowsUpdated()
            .block()
    }

    private fun R2dbcEntityTemplate.insertTestData() {
        insert(1000123, 978, BigDecimal(0.00), OPEN)
        insert(1000456, 840, BigDecimal(1002.00), OPEN)
        insert(1000678, 978, BigDecimal(345.00), OPEN)
        insert(1000236, 978, BigDecimal(0.00), CLOSED)
    }

    private fun R2dbcEntityTemplate.insert(
        accountNumber: Long,
        currency: Int,
        balance: BigDecimal,
        status: AccountStatus
    ) =
        insert<AccountDetails>()
            .into("account")
            .using(AccountDetails(accountNumber, currency, balance, status))
            .block(Duration.ofMillis(50))
            ?: throw RuntimeException("Can't initialize DB")
}
