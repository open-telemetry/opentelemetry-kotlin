package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceFlagsImplTest {

    private val factory = TraceFlagsFactoryImpl()

    @Test
    fun testEmptyString() {
        val flags = factory.fromHex("")

        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("00", flags.hex)
    }

    @Test
    fun testSingleChar() {
        val flags = factory.fromHex("1")

        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("00", flags.hex)
    }

    @Test
    fun testValidTraceFlagsParsed() {
        val default = factory.fromHex("00")
        assertFalse(default.isRandom)
        assertFalse(default.isSampled)
        assertEquals("00", default.hex)

        val sampled = factory.fromHex("01")
        assertFalse(sampled.isRandom)
        assertTrue(sampled.isSampled)
        assertEquals("01", sampled.hex)

        val random = factory.fromHex("02")
        assertTrue(random.isRandom)
        assertFalse(random.isSampled)
        assertEquals("02", random.hex)

        val sampledAndRandom = factory.fromHex("03")
        assertTrue(sampledAndRandom.isRandom)
        assertTrue(sampledAndRandom.isSampled)
        assertEquals("03", sampledAndRandom.hex)
    }
}
