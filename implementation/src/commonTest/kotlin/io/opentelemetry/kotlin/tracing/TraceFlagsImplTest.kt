package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import kotlin.test.Test
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
    }

    @Test
    fun testSingleChar() {
        val flags = factory.fromHex("1")
        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun testValidTraceFlagsParsed() {
        val default = factory.fromHex("00")
        assertFalse(default.isRandom)
        assertFalse(default.isSampled)

        val sampled = factory.fromHex("01")
        assertFalse(sampled.isRandom)
        assertTrue(sampled.isSampled)

        val random = factory.fromHex("02")
        assertTrue(random.isRandom)
        assertFalse(random.isSampled)

        val sampledAndRandom = factory.fromHex("03")
        assertTrue(sampledAndRandom.isRandom)
        assertTrue(sampledAndRandom.isSampled)
    }
}
