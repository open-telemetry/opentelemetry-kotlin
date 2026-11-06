package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalApi::class)
internal class StatusDataTest {

    @Test
    fun testErrorEquality() {
        assertEquals(StatusData.Error("x"), StatusData.Error("x"))
        assertEquals(StatusData.Error("x").hashCode(), StatusData.Error("x").hashCode())
        assertEquals(StatusData.Error(null), StatusData.Error(null))
        assertNotEquals(StatusData.Error("x"), StatusData.Error("y"))
        assertNotEquals(StatusData.Error("x"), StatusData.Error(null))
        assertNotEquals<StatusData>(StatusData.Error(null), StatusData.Unset)
    }

    @Test
    fun testErrorToString() {
        assertEquals("Error(description=x)", StatusData.Error("x").toString())
        assertEquals("Error(description=null)", StatusData.Error(null).toString())
    }
}
