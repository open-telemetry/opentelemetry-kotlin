package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ContextExtTest {

    @Test
    fun testContext() {
        val root = FakeContext()
        val attrs = mapOf(
            "a" to true,
            "b" to 1,
        )
        val firstCtx = root.with(attrs) as FakeContext
        assertEquals(attrs, firstCtx.findAttrs())

        val extra = mapOf(
            "a" to "test",
            "c" to "test",
        )
        val secondCtx = firstCtx.with(extra) as FakeContext
        val expected = mapOf(
            "a" to "test",
            "b" to 1,
            "c" to "test",
        )
        assertEquals(expected, secondCtx.findAttrs())

        val thirdCtx = secondCtx.with(emptyMap()) as FakeContext
        assertEquals(expected, thirdCtx.findAttrs())
    }

    private fun FakeContext.findAttrs(): Map<String, Any?> = attrs.mapKeys { it.key.name }
}
