package com.transfers.internal.rest.module

import com.transfers.internal.log
import com.transfers.internal.rest.dto.InternalTransactionRequestDto
import com.transfers.internal.rest.dto.toModel
import com.transfers.internal.service.InternalTransferService
import com.transfers.internal.util.validateBy
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.koin.ktor.ext.inject
import javax.validation.Validator

fun Application.internalTransferModule() {

    val internalTransferService: InternalTransferService by inject()
    val validator: Validator by inject()

    routing {
        post("/internal/transfer/") {
            val internalTransactionDto = call.receive<InternalTransactionRequestDto>()
            internalTransactionDto.validateBy(validator)
            log.info("process=process_transaction status=request_validated")
            internalTransferService.processTransaction(internalTransactionDto.toModel())
            call.respond(HttpStatusCode.OK)
        }
    }

}