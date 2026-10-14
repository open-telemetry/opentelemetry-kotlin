package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalApi::class)
internal class ContextKeyEntrypointTest {

    @Test
    fun `same named keys are distinct`() {
        assertNotEquals(createContextKey<String>("key"), createContextKey<String>("key"))
    }

    @Test
    fun `key is equal to itself`() {
        val key = createContextKey<String>("key")
        assertEquals(key, key)
    }

    @Test
    fun `name is used for debugging`() {
        assertEquals("my_key", createContextKey<String>("my_key").toString())
    }
}
