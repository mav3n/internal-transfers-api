package com.transfers.internal.repository

import com.transfers.internal.rest.exception.InsufficientBalanceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AccountRepositoryTest {

    private val underTest = AccountRepository()

    @Test
    fun `should check if accounts exists correctly`() {
        // given
        val accounts = underTest.accounts()

        // then
        accounts.forEach { account ->
            assertTrue { underTest.exists(account.id) }
        }
        assertFalse(underTest.exists(UUID.randomUUID()))
    }

    @Test
    fun `should find account by Id`() {
        // given
        val account = underTest.createAccount(BigDecimal(100))

        // when
        val response = underTest.accountById(account.id)!!

        // then
        assertThat(response.id).isEqualTo(account.id)
        assertThat(response.balance).isEqualTo(account.balance)
    }

    @Test
    fun `should return all accounts`() {
        // when
        val response = underTest.accounts()

        // then
        assertNotNull(response)
        assertThat(response.size).isGreaterThanOrEqualTo(10)
    }

    @Test
    fun `should create account`() {
        // when
        val account = underTest.createAccount(BigDecimal(100))

        // then
        assertTrue(underTest.exists(account.id))
    }

    @Test
    fun `should debit account correctly`() {
        // given
        val account = underTest.createAccount(BigDecimal(100))

        // when
        underTest.debitBalance(account.id, BigDecimal(60))

        // then
        assertThat(underTest.accountById(account.id)?.balance).isEqualTo(BigDecimal(40))
    }


    @Test
    fun `should credit account correctly`() {
        // given
        val account = underTest.createAccount(BigDecimal(100))

        // when
        underTest.creditBalance(account.id, BigDecimal(60))

        // then
        assertThat(underTest.accountById(account.id)?.balance).isEqualTo(BigDecimal(160))
    }

    @Test
    fun `should fail debiting account if debit amount is greater than balance`() {
        // given
        val account = underTest.createAccount(BigDecimal(100))

        // when
        assertThrows(InsufficientBalanceException::class.java) {
            underTest.debitBalance(account.id, BigDecimal(160))
        }

        // then
        assertThat(underTest.accountById(account.id)?.balance).isEqualTo(BigDecimal(100))
    }

}