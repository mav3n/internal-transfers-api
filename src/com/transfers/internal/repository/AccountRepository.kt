package com.transfers.internal.repository

import com.transfers.internal.model.Account
import com.transfers.internal.rest.exception.AccountNotFoundException
import com.transfers.internal.rest.exception.InsufficientBalanceException
import com.transfers.internal.util.normalize
import java.lang.IllegalStateException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

open class AccountRepository {

    open fun exists(id: UUID): Boolean = ACCOUNTS[id] != null

    open fun accountById(id: UUID) = ACCOUNTS[id]

    open fun accounts() = ACCOUNTS.values.sortedBy { it.id }

    open fun createAccount(balance: BigDecimal): Account =
        createAccountId().let { id -> Account(id, balance).also { ACCOUNTS[id] = it } }

    @Throws(InsufficientBalanceException::class, AccountNotFoundException::class)
    open fun debitBalance(accountId: UUID, amount: BigDecimal) {
        synchronized(lock(accountId)) {
            ACCOUNTS[accountId]?.let {
                if (it.balance >= amount) {
                    it.balance -= amount
                } else {
                    throw InsufficientBalanceException("Sender Account has insufficient balance")
                }
            } ?: throw AccountNotFoundException("Account not found with Id:$accountId")
        }
    }

    @Throws(AccountNotFoundException::class)
    open fun creditBalance(accountId: UUID, amount: BigDecimal) {
        synchronized(lock(accountId)) {
            ACCOUNTS[accountId]?.let {
                it.balance += amount
            } ?: throw AccountNotFoundException("Account not found with Id:$accountId")
        }
    }

    private fun createAccountId(): UUID {
        var id = UUID.randomUUID()
        while (ACCOUNTS.containsKey(id)) {
            id = UUID.randomUUID()
        }
        return id
    }

    companion object {
        private val ACCOUNTS = ConcurrentHashMap<UUID, Account>()
        /**
         * LOCKS can be replaced by `advisory locks` if we use relational database instead of ConcurrentHashMap
         * for data storage
         */
        private val LOCKS = ConcurrentHashMap<Int, Any>()

        private const val LOCK_SIZE = 100

        init {
            /**
             * Initializing Accounts data-store with some random accounts
             * for the ease of using the transfer-api directly
             */
            for (id in randomIds()) {
                ACCOUNTS[id] = Account(
                    id,
                    BigDecimal(Random.nextDouble(1_000.0)).setScale(2, RoundingMode.UP)
                )
            }

            /**
             * Initializing locks
             */
            (1..LOCK_SIZE).forEach { LOCKS[it - 1] = Any() }
        }

        private fun randomIds(): List<UUID> = (1..10).map { UUID.randomUUID() }

        private fun lock(id: UUID): Any =
            id.normalize(LOCK_SIZE).let { LOCKS[it] } ?: throw IllegalStateException("Lock should have been present!")

    }

}


