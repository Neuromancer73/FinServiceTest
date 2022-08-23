package com.finservicetest.configuration

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
class WriteLockTest(@Autowired private val client: WebTestClient) {

    @Test
    fun contextLoads() {
        runBlocking {



        }
    }
}
