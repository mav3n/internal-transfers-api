package com.transfers.internal.service

import com.transfers.internal.repository.AccountRepository
import org.koin.dsl.module
import javax.validation.Validation
import javax.validation.Validator

/**
 * Bean Definitions to be managed by Koin
 */
val beanDefinitionsModule = module {
    single { AccountRepository() }
    single<AccountService> { AccountService(get()) }
    single<InternalTransferService> { InternalTransferService(get()) }
    single<Validator> { Validation.buildDefaultValidatorFactory().validator }
}