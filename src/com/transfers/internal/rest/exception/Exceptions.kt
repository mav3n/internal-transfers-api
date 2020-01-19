package com.transfers.internal.rest.exception

class AccountNotFoundException(override val message: String) : RuntimeException(message)

class BadTransactionRequestException(override val message: String) : RuntimeException(message)

class InsufficientBalanceException(override val message: String) : RuntimeException(message)