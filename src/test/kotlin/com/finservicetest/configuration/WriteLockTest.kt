package com.finservicetest.configuration

import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.model.AccountOperation
import com.finservicetest.domain.model.AccountOperationType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal


@WebFluxTest
class WriteLockTest(@Autowired private val client: WebTestClient) {

    private fun assertAccountBalance(accountNumber: Long, expectedBalance: BigDecimal) =
        client
            .get()
            .uri("/api/account/$accountNumber/balance")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AccountDetails::class.java)
            .value {
                val accountBalance = it.balance
                println("Balance: $accountBalance")
                assert(expectedBalance == accountBalance)
            }

    private fun assertUpdateResponseOk(accountOperation: AccountOperation) {
        client
            .patch()
            .uri("/api/operation")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(accountOperation)
            .exchange()
            .expectStatus()
            .isOk
    }

    private fun callUpdate(accountOperation: AccountOperation) {
        client
            .patch()
            .uri("/api/operation")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(accountOperation)
            .exchange()

    }

    @Test
    fun `update balance of concurrently - increment`() {
        runBlocking {
            assertAccountBalance(1000123, BigDecimal.ZERO)
            (1..1000).map {
                async {
                    assertUpdateResponseOk(
                        AccountOperation(1000123, 978, BigDecimal.ONE, AccountOperationType.CREDIT)
                    )
                }
            }.awaitAll()
            assertAccountBalance(1000123, BigDecimal(1000))
        }
    }

    @Test
    fun `update balance of concurrently - decrement`() {
        runBlocking {
            assertAccountBalance(1000678, BigDecimal(345))
            (1..300).map {
                async {
                    callUpdate(
                        AccountOperation(1000678, 978, BigDecimal.ONE, AccountOperationType.DEBIT)
                    )
                }
            }.awaitAll()
            assertAccountBalance(1000678, BigDecimal(45))
        }
    }

    @Test
    fun `update balance of concurrently - decrement to zero`() {
        runBlocking {
            assertAccountBalance(1000456, BigDecimal(1002))
            (1..2000).map {
                async {
                    callUpdate(AccountOperation(1000456, 840, BigDecimal.ONE, AccountOperationType.DEBIT))
                }
            }.awaitAll()
            assertAccountBalance(1000456, BigDecimal(0))
        }
    }
}
