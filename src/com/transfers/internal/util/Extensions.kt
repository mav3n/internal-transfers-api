package com.transfers.internal.util

import com.transfers.internal.rest.exception.BadTransactionRequestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.ConstraintViolation
import javax.validation.Validator
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

fun loggerOf(clazz: KClass<*>): Logger = LoggerFactory.getLogger(clazz.java)

@Throws(BadTransactionRequestException::class)
fun <T : Any> T.validateBy(validator: Validator) {
    validator.validate(this)
        .takeIf { it.isNotEmpty() }
        ?.let { throw BadTransactionRequestException(it.first().messageWithFieldName()) }
}

fun <T : Any> ConstraintViolation<T>.messageWithFieldName() = "${this.propertyPath} ${this.message}"

fun UUID.normalize(mod: Int) =
    require(mod > 0) { "mod must be greater than 0" }.let { this.hashCode().absoluteValue % mod }