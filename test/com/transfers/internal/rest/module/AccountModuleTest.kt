package com.transfers.internal.rest.module

import com.transfers.internal.model.Account
import com.transfers.internal.module
import com.transfers.internal.rest.dto.AccountRequestDto
import com.transfers.internal.rest.dto.ErrorDto
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
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AccountModuleTest {

    @Test
    fun `get accounts returns at least 1 account`() {
        withTestApplication({ module() }) {
            handleRequest(HttpMethod.Get, "/accounts").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue { response.contentOrEmpty.contains("id") }
                assertTrue { response.contentOrEmpty.contains("balance") }
            }
        }
    }

    @Test
    fun `should create account successfully`() {
        withTestApplication({ module() }) {
            handleRequest(HttpMethod.Post, "/accounts/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    AccountRequestDto(
                        balance = BigDecimal(100)
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
                response.contentOrEmpty.fromJson<Account>().let {
                    assertEquals(BigDecimal(100), it.balance)
                    assertNotNull(it.id)
                }
            }
        }
    }

    @Test
    fun `should error when sender account id is null in transfer request`() {
        withTestApplication({ module() }) {
            handleRequest(HttpMethod.Post, "/accounts/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    AccountRequestDto(
                        balance = null
                    ).toJson()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                response.contentOrEmpty.fromJson<ErrorDto>().let {
                    assertEquals(400, it.errorCode)
                    assertEquals("balance must not be null", it.message)
                }
            }
        }
    }
}