package com.transfers.internal.rest.module

import com.transfers.internal.appModules
import com.transfers.internal.model.Account
import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.dto.ErrorDto
import com.transfers.internal.rest.dto.InternalTransactionRequestDto
import com.transfers.internal.service.beanDefinitionsModule
import com.transfers.internal.util.contentOrEmpty
import com.transfers.internal.util.fromJson
import com.transfers.internal.util.toJson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalTransferModuleTest : KoinTest {

    private lateinit var account1: Account
    private lateinit var account2: Account

    private val accountRepository by inject<AccountRepository>()

    @BeforeEach
    fun setup() {
        stopKoin()
        startKoin { modules(beanDefinitionsModule) }
        account1 = accountRepository.createAccount(BigDecimal(100))
        account2 = accountRepository.createAccount(BigDecimal(100))
    }

    @AfterAll
    fun cleanup() {
        stopKoin()
    }

    @Test
    fun `should process internal transfer successfully`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = account1.id,
                        senderAccountId = account2.id,
                        amount = BigDecimal(10)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(BigDecimal(110), accountRepository.accountById(account1.id)!!.balance)
                assertEquals(BigDecimal(90), accountRepository.accountById(account2.id)!!.balance)
            }
        }
    }

    @Test
    fun `should return error when receiver account id is null`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = null,
                        senderAccountId = account2.id,
                        amount = BigDecimal(10)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("receiverAccountId must not be null", it.message)
                }
            }
        }
    }

    @Test
    fun `should return error when sender account id is null`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = account1.id,
                        senderAccountId = null,
                        amount = BigDecimal(10)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("senderAccountId must not be null", it.message)
                }
            }
        }
    }

    @Test
    fun `should return error when amount is null`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = account1.id,
                        senderAccountId = account2.id,
                        amount = null
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("amount must not be null", it.message)
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(longs = [0, -10])
    fun `should return error when amount is less than or equal to zero`(amount: Long) {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = account1.id,
                        senderAccountId = account2.id,
                        amount = BigDecimal(amount)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("Amount to be transferred should be greater than 0", it.message)
                }
            }
        }
    }

    @Test
    fun `should return error when sender and receiver are same`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = account1.id,
                        senderAccountId = account1.id,
                        amount = BigDecimal(10)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("Sender and Receiver accounts cannot be same", it.message)
                }
            }
        }
    }

    @Test
    fun `should return error when account not found`() {
        val receiverAccountId = UUID.randomUUID()
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = receiverAccountId,
                        senderAccountId = account2.id,
                        amount = BigDecimal(10)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(404, it.errorCode)
                    assertEquals("Account not found with Id:$receiverAccountId", it.message)
                }
            }
        }
    }

    @Test
    fun `should return error when sender account has lower than required balance`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    InternalTransactionRequestDto(
                        receiverAccountId = account1.id,
                        senderAccountId = account2.id,
                        amount = BigDecimal(110)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("Sender Account has insufficient balance", it.message)
                }
            }
        }
    }

    @Test
    fun `should return error when invalid UUID is passed as accountId`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """
                        {
                            "senderAccountId": "invalid-UUID",
                            "receiverAccountId": "c2b747d4-9eb6-4a2f-8685-40609ad875d5",
                            "amount": 1200
                        }
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertTrue(
                        it.message
                            .contains("UUID has to be represented by standard 36-char representation")
                    )
                }
            }
        }
    }

    @Test
    fun `should return error when invalid JSON is passed as input`() {
        withTestApplication({ appModules() }) {
            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """
                        { invalid-Json }
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("Invalid JSON. Provide a valid JSON as input", it.message)
                }
            }
        }
    }

    @Test
    fun `should process concurrent internal transfers successfully`() {
        account1 = accountRepository.createAccount(BigDecimal(1000))
        account2 = accountRepository.createAccount(BigDecimal(1000))
        withTestApplication({ appModules() }) {
            runBlocking {
                coroutineScope {
                    (1..1000).map {
                        GlobalScope.async {
                            handleRequest(HttpMethod.Post, "/internal/transfer/") {
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                setBody(
                                    if (it % 2 == 0) {
                                        InternalTransactionRequestDto(
                                            receiverAccountId = account1.id,
                                            senderAccountId = account2.id,
                                            amount = BigDecimal(2)
                                        )
                                    } else {
                                        InternalTransactionRequestDto(
                                            receiverAccountId = account2.id,
                                            senderAccountId = account1.id,
                                            amount = BigDecimal(1)
                                        )
                                    }.toJson()
                                )
                            }.apply { }
                        }
                    }.awaitAll()
                }
            }
            assertEquals(BigDecimal(1500), accountRepository.accountById(account1.id)!!.balance)
            assertEquals(BigDecimal(500), accountRepository.accountById(account2.id)!!.balance)
        }
    }
}


