package io.opentelemetry.kotlin

import org.junit.Test
import kotlin.test.assertEquals

internal class DroppedCountTest {

    @Test
    fun `returns difference between total and retained`() {
        assertEquals(3, droppedCount(total = 5, retained = 2))
    }

    @Test
    fun `returns zero when nothing was dropped`() {
        assertEquals(0, droppedCount(total = 2, retained = 2))
    }

    @Test
    fun `never returns a negative count`() {
        assertEquals(0, droppedCount(total = 1, retained = 4))
    }
}
