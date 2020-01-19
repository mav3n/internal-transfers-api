package com.transfers.internal.service

import com.transfers.internal.repository.AccountRepository
import com.transfers.internal.rest.dto.AccountRequestDto

class AccountService(private val accountRepository: AccountRepository) {

    fun accounts() = accountRepository.accounts()

    fun createAccount(accountRequest: AccountRequestDto) =
        accountRepository.createAccount(accountRequest.balance!!)

}