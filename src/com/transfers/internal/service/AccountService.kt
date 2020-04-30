package com.transfers.internal.service

import com.transfers.internal.model.Account
import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.dto.AccountRequestDto
import com.transfers.internal.util.loggerOf

class AccountService(private val accountRepository: AccountRepository) {

    private val log = loggerOf(AccountService::class)

    fun accounts() = accountRepository.accounts()

    fun createAccount(accountRequest: AccountRequestDto): Account =
        accountRequest.balance?.let {
            log.info("process=create_account balance=${accountRequest.balance}")
            accountRepository.createAccount(accountRequest.balance)
        } ?: throw IllegalStateException("Balance can't be null")

}