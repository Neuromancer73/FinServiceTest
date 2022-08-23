package com.finservicetest

import com.finservicetest.api.AccountRouterConfiguration
import com.finservicetest.domain.data.AccountRepository
import com.finservicetest.domain.model.AccountDetails
import com.finservicetest.domain.model.AccountStatus
import com.ninjasquad.springmockk.MockkBean
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@WebFluxTest
@SpringBootTest(classes = [com.finservicetest.configuration.FinServiceTestApplication::class])
@Import(AccountRouterConfiguration::class)
class EndpointsTest(@Autowired private val client: WebTestClient) {

    @MockkBean
    private lateinit var accountRepository: AccountRepository

    private val openAccountDetails =
        AccountDetails(100, 100, BigDecimal("5000.00"), AccountStatus.OPEN)

    private val closedAccountDetails =
        AccountDetails(200, 200, BigDecimal("0.00"), AccountStatus.CLOSED)

    @Test
    fun `get status of open account success`() {
        coEvery {
            accountRepository.getAccountDetails(any())
        } returns openAccountDetails

        client
            .get()
            .uri("/api/account/100/status")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .toStr()


//            .json(
//                """{
//                    "accountNumber": 1000456,
//                    "currency": 840,
//                    "balance": 997,
//                    "status": "OPEN"
//                }"""
//            )
//
//        /balance
    }


}
