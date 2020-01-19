package com.transfers.internal

import com.fasterxml.jackson.databind.SerializationFeature
import com.transfers.internal.rest.module.accountsModule
import com.transfers.internal.rest.module.internalTransferModule
import com.transfers.internal.rest.exception.exceptionHandler
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    exceptionHandler()

    accountsModule()
    internalTransferModule()

}