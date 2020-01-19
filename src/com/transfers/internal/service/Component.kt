package com.transfers.internal.service

import com.transfers.internal.repository.AccountRepository
import javax.validation.Validation
import javax.validation.Validator

/**
 * Using a Singleton class `Component` which instantiates all the shared components for entire application.
 * (Simple alternative for an Dependency Injection Framework)
 */
object Component {
    val accountRepository = AccountRepository()
    val accountService = AccountService(accountRepository)
    val internalTransferService = InternalTransferService(accountRepository)
    val validator: Validator = Validation.buildDefaultValidatorFactory().validator
}