package com.transfers.internal.service

import com.transfers.internal.model.InternalTransaction
import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.exception.AccountNotFoundException
import com.transfers.internal.rest.exception.BadTransactionRequestException
import com.transfers.internal.rest.exception.InsufficientBalanceException
import com.transfers.internal.util.loggerOf
import java.math.BigDecimal
import java.util.UUID

class InternalTransferService(private val accountRepository: AccountRepository) {

    private val log = loggerOf(InternalTransferService::class)

    fun processTransaction(transaction: InternalTransaction) {
        try {
            validateTransaction(transaction)
            accountRepository.debitBalance(transaction.senderAccountId, transaction.amount)
            log.info("process=process_transaction status=debited_from account_id=${transaction.senderAccountId} " +
                        "amount=${transaction.amount}")
            try {
                accountRepository.creditBalance(transaction.receiverAccountId, transaction.amount)
                log.info("process=process_transaction status=credited_to account_id=${transaction.senderAccountId} " +
                            "amount=${transaction.amount}")
            } catch (e: Exception) {
                // Rollback the debit if credit fails
                accountRepository.creditBalance(transaction.senderAccountId, transaction.amount)
                throw e
            }
        } catch (e: Exception) {
            when (e) {
                is BadTransactionRequestException,
                is AccountNotFoundException,
                is InsufficientBalanceException -> throw e
                else -> {
                    log.error(
                        "process=process_transaction status=failed senderAccountId=${transaction.senderAccountId} " +
                                "receiverAccountId=${transaction.receiverAccountId}", e
                    )
                    throw e
                }
            }
        }
    }

    @Throws(BadTransactionRequestException::class, AccountNotFoundException::class)
    private fun validateTransaction(transaction: InternalTransaction) {
        if (transaction.receiverAccountId == transaction.senderAccountId) {
            throw BadTransactionRequestException("Sender and Receiver accounts cannot be same")
        }
        if (transaction.amount <= BigDecimal.ZERO) {
            throw BadTransactionRequestException("Amount to be transferred should be greater than 0")
        }
        validateAccount(transaction.receiverAccountId)
        validateAccount(transaction.senderAccountId)
    }

    @Throws(AccountNotFoundException::class)
    private fun validateAccount(id: UUID) {
        if (!accountRepository.exists(id)) {
            throw AccountNotFoundException("Account not found with Id:$id")
        }
    }
}


