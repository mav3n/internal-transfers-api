package com.transfers.internal

import com.fasterxml.jackson.databind.SerializationFeature
import com.transfers.internal.rest.exception.exceptionHandler
import com.transfers.internal.rest.module.accountsModule
import com.transfers.internal.rest.module.internalTransferModule
import com.transfers.internal.service.beanDefinitionsModule
import com.transfers.internal.util.loggerOf
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import org.koin.ktor.ext.Koin
import org.slf4j.Logger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {
    appBeans()
    appModules()
}

fun Application.appModules() {
    jacksonModule()
    exceptionHandler()

    // Controllers or Routes
    accountsModule()
    internalTransferModule()
}

private fun Application.jacksonModule() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
}

private fun Application.appBeans() {
    install(Koin) {
        printLogger()
        modules(beanDefinitionsModule)
    }
    log.info("Installed Koin Module and loaded dependencies in KoinContext")
}

val Application.log: Logger
    get() = loggerOf(Application::class)