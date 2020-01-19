package com.transfers.internal.util

import com.google.gson.Gson
import org.mockito.Mockito
import java.math.BigDecimal
import java.util.UUID

fun <T : Any> T.toJson(): String = Gson().toJson(this)

inline fun <reified T : Any> String.fromJson(): T = Gson().fromJson(this, T::class.java)

// MockitoWrapper with work-around to support Kotlin non-nullable variables
inline fun <reified T : Any> any(default: T): T = Mockito.any(T::class.java) ?: default

fun anyUUID() = any<UUID>(UUID.randomUUID())

fun anyBigDecimal() = any<BigDecimal>(BigDecimal.ZERO)