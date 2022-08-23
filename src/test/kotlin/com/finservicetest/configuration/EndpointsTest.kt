package com.finservicetest.configuration

import com.finservicetest.domain.data.AccountRepository
import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.model.AccountOperation
import com.finservicetest.domain.model.AccountOperationType
import com.finservicetest.domain.model.AccountStatus
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@WebFluxTest
class EndpointsTest(@Autowired private val client: WebTestClient) {

    @MockkBean
    private lateinit var accountRepository: AccountRepository

    private val openedAccountDetails =
        AccountDetails(100, 100, BigDecimal("5000.0000001"), AccountStatus.OPEN)

    private val closedAccountDetails =
        AccountDetails(200, 200, BigDecimal("0.00"), AccountStatus.CLOSED)

    @Test
    fun `get status of opened account success`() {
        coEvery {
            accountRepository.getAccountDetails(100)
        } returns openedAccountDetails
        client
            .get()
            .uri("/api/account/100/status")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .isEqualTo(AccountStatus.OPEN.name)
    }

    @Test
    fun `get status of closed account success`() {
        coEvery {
            accountRepository.getAccountDetails(200)
        } returns closedAccountDetails
        client
            .get()
            .uri("/api/account/200/status")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .isEqualTo(AccountStatus.CLOSED.name)
    }

    @Test
    fun `get balance of opened account success`() {
        coEvery {
            accountRepository.getAccountDetails(200)
        } returns openedAccountDetails
        client
            .get()
            .uri("/api/account/200/balance")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AccountDetails::class.java)
            .isEqualTo(openedAccountDetails)
    }

    @Test
    fun `get balance of closed account success`() {
        coEvery {
            accountRepository.getAccountDetails(200)
        } returns closedAccountDetails
        client
            .get()
            .uri("/api/account/200/balance")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AccountDetails::class.java)
            .isEqualTo(closedAccountDetails)
    }

    @Test
    fun `get status of non-existent account failure`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns null
        client
            .get()
            .uri("/api/account/300/status")
            .exchange()
            .expectStatus()
            .isBadRequest
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

    @Test
    fun `update balance of opened account success - decrease to zero`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        coEvery {
            accountRepository.updateAccountBalance(any())
        } returns true
        assertUpdateResponseOk(AccountOperation(100, 100, BigDecimal(5000.0000001), AccountOperationType.DEBIT))
    }

    @Test
    fun `update balance of opened account success - add zero`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        coEvery {
            accountRepository.updateAccountBalance(any())
        } returns true
        assertUpdateResponseOk(AccountOperation(100, 100, BigDecimal.ZERO, AccountOperationType.CREDIT))
    }

    @Test
    fun `update balance of opened account success - add one`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        coEvery {
            accountRepository.updateAccountBalance(any())
        } returns true
        assertUpdateResponseOk(AccountOperation(100, 100, BigDecimal.ONE, AccountOperationType.CREDIT))
    }


    private fun assertUpdateBadRequest(accountOperation: AccountOperation) {
        client
            .patch()
            .uri("/api/operation")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(accountOperation)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `update balance of closed account failure - closed`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns closedAccountDetails
        assertUpdateBadRequest(AccountOperation(200, 200, BigDecimal.ZERO, AccountOperationType.CREDIT))
    }

    @Test
    fun `update balance of wrong account failure - wrong account number`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        assertUpdateBadRequest(AccountOperation(300, 200, BigDecimal.ZERO, AccountOperationType.CREDIT))
    }

    @Test
    fun `update balance of opened account failure - negative amount`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        assertUpdateBadRequest(AccountOperation(100, 100, BigDecimal(-1), AccountOperationType.CREDIT))
    }

    @Test
    fun `update balance of opened account failure - wrong currency`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        coEvery {
            accountRepository.updateAccountBalance(any())
        } returns true
        assertUpdateBadRequest(AccountOperation(100, 500, BigDecimal(5000.0000002), AccountOperationType.DEBIT))
    }

    @Test
    fun `update balance of opened account failure - insufficient funds`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openedAccountDetails
        coEvery {
            accountRepository.updateAccountBalance(any())
        } returns true
        assertUpdateBadRequest(AccountOperation(100, 100, BigDecimal(5000.0000002), AccountOperationType.DEBIT))
    }

}
