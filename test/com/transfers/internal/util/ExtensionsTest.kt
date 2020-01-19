package com.transfers.internal.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class ExtensionsTest {

    @Test
    fun `should fail to normalize UUID if mod is 0`() {
        // given
        val id = UUID.randomUUID()

        // then
        assertThrows(IllegalArgumentException::class.java) {
            id.normalize(0)
        }.also { assertThat(it.message).isEqualTo("mod must be greater than 0") }
    }

    @Test
    fun `should normalize the UUID within the specified limit`() {
        (1..1000).map { UUID.randomUUID() }.forEach {
            assertThat(it.normalize(100)).isBetween(0, 99)
        }
    }

}