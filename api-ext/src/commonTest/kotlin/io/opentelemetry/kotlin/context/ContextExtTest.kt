package io.opentelemetry.kotlin.context

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ContextExtTest {

    @Test
    fun testAsImplicitContext() {
        var attached = false
        var detached = false
        val ctx = FakeContext(
            onAttach = { attached = true },
            onDetach = {
                detached = true
                true
            }
        )

        val expected = "result"
        val result = ctx.asImplicitContext {
            expected
        }

        assertEquals(expected, result)
        assertTrue(attached)
        assertTrue(detached)
    }

    @Test
    fun testAsImplicitContextDetachesOnException() {
        var attached = false
        var detached = false
        val ctx = FakeContext(
            onAttach = { attached = true },
            onDetach = {
                detached = true
                true
            }
        )

        assertFailsWith<IllegalStateException> {
            ctx.asImplicitContext {
                throw IllegalStateException("test exception")
            }
        }
        assertTrue(attached)
        assertTrue(detached)
    }
}
