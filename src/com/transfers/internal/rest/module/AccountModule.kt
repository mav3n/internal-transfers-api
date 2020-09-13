package com.transfers.internal.rest.module

import com.transfers.internal.log
import com.transfers.internal.rest.dto.AccountRequestDto
import com.transfers.internal.service.AccountService
import com.transfers.internal.util.validateBy
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import org.koin.ktor.ext.inject
import javax.validation.Validator


fun Application.accountsModule() {

    val accountService: AccountService by inject()
    val validator: Validator by inject()

    routing {
        route("/accounts") {
            get {
                call.respond(accountService.accounts())
            }
            post {
                val accountRequestDto = call.receive<AccountRequestDto>()
                accountRequestDto.validateBy(validator)
                log.info("process=create_account status=request_validated")
                call.respond(HttpStatusCode.Created, accountService.createAccount(accountRequestDto))
            }
        }
    }

}