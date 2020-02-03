package com.transfers.internal.service

import com.transfers.internal.model.Account
import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.dto.AccountRequestDto

class AccountService(private val accountRepository: AccountRepository) {

    fun accounts() = accountRepository.accounts()

    fun createAccount(accountRequest: AccountRequestDto): Account =
        accountRequest.balance?.let {
            accountRepository.createAccount(accountRequest.balance)
        } ?: throw IllegalStateException("Balance can't be null")

}