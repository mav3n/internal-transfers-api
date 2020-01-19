package com.transfers.internal.rest.module

import com.transfers.internal.rest.dto.AccountRequestDto
import com.transfers.internal.service.Component
import com.transfers.internal.util.validate
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing


fun Application.accountsModule() {

    val accountService = Component.accountService

    routing {

        route("/accounts") {
            get {
                accountService.accounts()
                call.respond(accountService.accounts())
            }

            post {
                val accountRequestDto = call.receive<AccountRequestDto>()
                accountRequestDto.validate()
                call.respond(HttpStatusCode.OK, accountService.createAccount(accountRequestDto))
            }
        }

    }

}