package com.transfers.internal.service

import com.transfers.internal.model.Account
import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.dto.AccountRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito
import java.math.BigDecimal
import java.util.UUID

open class AccountServiceTest {

    private val accountRepositoryMock: AccountRepository = Mockito.mock(AccountRepository::class.java)

    private var underTest = AccountService(accountRepositoryMock)

    @Test
    fun `should return all accounts`() {
        // given
        val accounts = listOf(
            Account(UUID.randomUUID(), BigDecimal(100)),
            Account(UUID.randomUUID(), BigDecimal(50))
        )
        given(accountRepositoryMock.accounts()).will { accounts }

        // when
        val response = underTest.accounts()

        // then
        verify(accountRepositoryMock).accounts()
        assertThat(response).isEqualTo(accounts)
    }


    @Test
    fun `should create account`() {
        // given
        val balance = BigDecimal(100)
        val account = Account(UUID.randomUUID(), balance)

        given(accountRepositoryMock.createAccount(balance)).will { account }

        // when
        val response = underTest.createAccount(AccountRequestDto(balance))

        // then
        verify(accountRepositoryMock).createAccount(balance)
        assertThat(response).isEqualTo(account)
    }

}