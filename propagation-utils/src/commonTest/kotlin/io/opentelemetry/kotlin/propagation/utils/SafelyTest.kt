package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SafelyTest {

    @Test
    fun `safely runs the action`() {
        var ran = false
        safely { ran = true }
        assertTrue(ran)
    }

    @Test
    fun `safely swallows anything thrown`() {
        safely { error("boom") }
    }

    @Test
    fun `safely with default returns the action result`() {
        assertEquals("result", safely("default") { "result" })
    }

    @Test
    fun `safely with default returns the default when the action throws`() {
        assertEquals("default", safely("default") { error("boom") })
    }
}
