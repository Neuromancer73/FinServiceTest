package com.finservicetest.api

import com.finservicetest.domain.model.AccountOperation
import com.finservicetest.domain.usecase.AccountUseCases
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*


private const val ACCOUNT_NUMBER_PATH_VARIABLE = "id"

@Configuration
class AccountRouterConfiguration {

    @Bean
    fun routes(accountEndpoints: AccountEndpoints) = coRouter {
        "/api".nest {
            accept(APPLICATION_JSON).nest {
                "/account".nest {
                    GET("/{$ACCOUNT_NUMBER_PATH_VARIABLE}/status", accountEndpoints::checkAccountStatus)
                    GET("/{$ACCOUNT_NUMBER_PATH_VARIABLE}/balance", accountEndpoints::enquireAccountBalance)
                }
                contentType(APPLICATION_JSON).nest {
                    PATCH("/operation", accountEndpoints::performOperationOnAccount)
                }
            }
        }
    }

    @Component
    class AccountEndpoints(val accountUseCases: AccountUseCases) {

        suspend fun checkAccountStatus(request: ServerRequest) =
            request.getAccountNumberFromPathOrNull()
                ?.let { accountUseCases.checkAccountStatusUseCase(it).toServerResponse() }
                ?: badRequestAndAwait()

        suspend fun enquireAccountBalance(request: ServerRequest) =
            request.getAccountNumberFromPathOrNull()
                ?.let { accountUseCases.getAccountBalanceUseCase(it).toServerResponse() }
                ?: badRequestAndAwait()

        suspend fun performOperationOnAccount(request: ServerRequest) =
            request.getAccountOperationModelFromBodyOrNull()
                ?.let { accountUseCases.sideOperationOnAccountUseCase(it).toServerResponse() }
                ?: badRequestAndAwait()

        private fun ServerRequest.getAccountNumberFromPathOrNull(doOnError: (Exception) -> Unit = {}) =
            try {
                pathVariable(ACCOUNT_NUMBER_PATH_VARIABLE).toLong()
            } catch (exception: IllegalArgumentException) {
                doOnError(exception)
                null
            }

        private suspend fun ServerRequest.getAccountOperationModelFromBodyOrNull(doOnError: (Exception) -> Unit = {}) =
            try {
                awaitBody<AccountOperation>()
            } catch (exception: Exception) {
                doOnError(exception)
                null
            }

        private suspend fun <T> Result<T>.toServerResponse() =
            when {
                isSuccess ->
                    getOrNull()
                        ?.let { ok().contentType(APPLICATION_JSON).bodyValueAndAwait(it) }
                        ?: internalServerErrorAndAwait()

                isFailure ->
                    exceptionOrNull()
                        ?.let { badRequestAndAwait() }
                        ?: internalServerErrorAndAwait()

                else ->
                    internalServerErrorAndAwait()
            }

        private suspend fun badRequestAndAwait() = badRequest().buildAndAwait()

        private suspend fun internalServerErrorAndAwait() = status(500).buildAndAwait()
    }
}
