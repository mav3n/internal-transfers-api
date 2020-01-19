package com.transfers.internal.rest.exception

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.transfers.internal.rest.dto.ErrorDto
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

fun Application.exceptionHandler() {

    install(StatusPages) {

        exception<Throwable> { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpStatusCode.InternalServerError.let {
                    ErrorDto(
                        it.description,
                        it.value
                    )
                }
            )
        }

        exception<BadTransactionRequestException> { e ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorDto(e.message, HttpStatusCode.BadRequest.value)
            )

        }

        exception<InsufficientBalanceException> { e ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorDto(e.message, HttpStatusCode.BadRequest.value)
            )
        }

        exception<InvalidFormatException> { e ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorDto(
                    e.originalMessage,
                    HttpStatusCode.BadRequest.value
                )
            )
        }

        exception<JsonParseException> { e ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorDto(
                    "Invalid JSON. Provide a valid JSON as input",
                    HttpStatusCode.BadRequest.value
                )
            )
        }

        exception<AccountNotFoundException> { e ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorDto(e.message, HttpStatusCode.NotFound.value)
            )
        }

    }

}