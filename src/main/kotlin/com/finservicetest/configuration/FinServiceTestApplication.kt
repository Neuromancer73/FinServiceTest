package com.finservicetest.configuration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan


@ComponentScan(basePackages = ["com.finservicetest"])
@SpringBootApplication
class FinServiceTestApplication

fun main(args: Array<String>) {
    runApplication<FinServiceTestApplication>(*args)
}
