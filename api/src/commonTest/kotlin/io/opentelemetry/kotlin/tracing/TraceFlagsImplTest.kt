package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalApi::class)
internal class TraceFlagsImplTest {

    @Test
    fun testEquality() {
        val sampled = TraceFlagsImpl(isSampled = true, isRandom = false)
        assertEquals(sampled, TraceFlagsImpl(isSampled = true, isRandom = false))
        assertEquals(sampled.hashCode(), TraceFlagsImpl(isSampled = true, isRandom = false).hashCode())
        assertNotEquals(sampled, TraceFlagsImpl(isSampled = true, isRandom = true))
        assertNotEquals(sampled, TraceFlagsImpl(isSampled = false, isRandom = false))
    }
}
