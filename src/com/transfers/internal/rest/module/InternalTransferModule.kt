package com.transfers.internal.rest.module

import com.transfers.internal.service.Component
import com.transfers.internal.rest.dto.InternalTransactionRequestDto
import com.transfers.internal.rest.dto.toModel
import com.transfers.internal.util.validate
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing


fun Application.internalTransferModule() {

    val internalTransferService = Component.internalTransferService

    routing {
        post("/internal/transfer/") {
            val internalTransactionDto = call.receive<InternalTransactionRequestDto>()
            internalTransactionDto.validate()
            internalTransferService.processTransaction(internalTransactionDto.toModel())
            call.respond(HttpStatusCode.OK)
        }
    }

}