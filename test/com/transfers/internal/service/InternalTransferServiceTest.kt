package com.transfers.internal.service

import com.transfers.internal.model.InternalTransaction
import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.exception.AccountNotFoundException
import com.transfers.internal.rest.exception.InsufficientBalanceException
import com.transfers.internal.util.anyUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.times
import org.mockito.BDDMockito.verifyNoMoreInteractions
import org.mockito.Mockito
import org.mockito.Mockito.verify
import java.math.BigDecimal
import java.util.UUID

open class InternalTransferServiceTest {

    private val accountRepositoryMock: AccountRepository = Mockito.mock(AccountRepository::class.java)

    private var underTest = InternalTransferService(accountRepositoryMock)

    @Test
    fun `should debit and credit while processing transaction`() {
        // given
        val receiverAccountId = UUID.randomUUID()
        val senderAccountId = UUID.randomUUID()
        val amount = BigDecimal(100)
        val transactionRequest = InternalTransaction(receiverAccountId, senderAccountId, amount)

        given(accountRepositoryMock.exists(receiverAccountId)).will { true }
        given(accountRepositoryMock.exists(senderAccountId)).will { true }
        given(accountRepositoryMock.debitBalance(senderAccountId, amount)).will { Unit }
        given(accountRepositoryMock.creditBalance(receiverAccountId, amount)).will { Unit }

        // when
        underTest.processTransaction(transactionRequest)

        // then
        verify(accountRepositoryMock, times(2)).exists(anyUUID())
        verify(accountRepositoryMock, times(1)).debitBalance(senderAccountId, amount)
        verify(accountRepositoryMock, times(1)).creditBalance(receiverAccountId, amount)
    }

    @Test
    fun `should rollback debit when credit fails`() {
        // given
        val receiverAccountId = UUID.randomUUID()
        val senderAccountId = UUID.randomUUID()
        val amount = BigDecimal(100)
        val transactionRequest = InternalTransaction(receiverAccountId, senderAccountId, amount)

        given(accountRepositoryMock.exists(receiverAccountId)).will { true }
        given(accountRepositoryMock.exists(senderAccountId)).will { true }
        given(accountRepositoryMock.debitBalance(senderAccountId, amount)).will { Unit }
        given(accountRepositoryMock.creditBalance(receiverAccountId, amount)).willThrow(RuntimeException::class.java)
        given(accountRepositoryMock.creditBalance(senderAccountId, amount)).will { Unit }

        // then
        assertThrows(RuntimeException::class.java) {
            underTest.processTransaction(transactionRequest)
        }

        verify(accountRepositoryMock, times(2)).exists(anyUUID())
        verify(accountRepositoryMock, times(1)).debitBalance(senderAccountId, amount)
        verify(accountRepositoryMock, times(1)).creditBalance(receiverAccountId, amount)
        verify(accountRepositoryMock, times(1)).creditBalance(senderAccountId, amount)
    }

    @Test
    fun `should throw AccountNotFoundException when accountId doesn't exists`() {
        // given
        val receiverAccountId = UUID.randomUUID()
        val senderAccountId = UUID.randomUUID()
        val amount = BigDecimal(100)
        val transactionRequest = InternalTransaction(receiverAccountId, senderAccountId, amount)

        given(accountRepositoryMock.exists(receiverAccountId)).will { true }
        given(accountRepositoryMock.exists(senderAccountId)).will { false }

        // then
        val exception = assertThrows(AccountNotFoundException::class.java) {
            underTest.processTransaction(transactionRequest)
        }

        assertThat(exception.message).isEqualTo("Account not found with Id:$senderAccountId")
        verify(accountRepositoryMock, times(2)).exists(anyUUID())
        verifyNoMoreInteractions(accountRepositoryMock)
    }

    @Test
    fun `should throw InsufficientBalanceException when sender have lower balance`() {
        // given
        val receiverAccountId = UUID.randomUUID()
        val senderAccountId = UUID.randomUUID()
        val amount = BigDecimal(100)
        val transactionRequest = InternalTransaction(receiverAccountId, senderAccountId, amount)

        given(accountRepositoryMock.exists(receiverAccountId)).will { true }
        given(accountRepositoryMock.exists(senderAccountId)).will { true }
        given(accountRepositoryMock.debitBalance(senderAccountId, amount))
            .willThrow(InsufficientBalanceException::class.java)

        // then
        assertThrows(InsufficientBalanceException::class.java) {
            underTest.processTransaction(transactionRequest)
        }
        verify(accountRepositoryMock, times(2)).exists(anyUUID())
        verify(accountRepositoryMock, times(1)).debitBalance(senderAccountId, amount)
        verifyNoMoreInteractions(accountRepositoryMock)
    }
}