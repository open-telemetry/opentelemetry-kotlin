package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceFlagsFactoryImplTest {

    private val factory = TraceFlagsFactoryImpl()

    @Test
    fun testDefaultProperty() {
        val flags = factory.default

        assertTrue(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun testSampledOnly() {
        val flags = factory.create(sampled = true, random = false)

        assertTrue(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun testRandomOnly() {
        val flags = factory.create(sampled = false, random = true)

        assertFalse(flags.isSampled)
        assertTrue(flags.isRandom)
    }

    @Test
    fun testSampledAndRandom() {
        val flags = factory.create(sampled = true, random = true)

        assertTrue(flags.isSampled)
        assertTrue(flags.isRandom)
    }

    @Test
    fun testDefault() {
        val flags = factory.create(sampled = false, random = false)

        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun testFromHexWithValidStrings() {
        val default = factory.fromHex("00")
        assertFalse(default.isSampled)
        assertFalse(default.isRandom)

        val sampled = factory.fromHex("01")
        assertTrue(sampled.isSampled)
        assertFalse(sampled.isRandom)

        val random = factory.fromHex("02")
        assertFalse(random.isSampled)
        assertTrue(random.isRandom)

        val both = factory.fromHex("03")
        assertTrue(both.isSampled)
        assertTrue(both.isRandom)

        val anotherBoth = factory.fromHex("2f") // 00101111
        assertTrue(anotherBoth.isSampled)
        assertTrue(anotherBoth.isRandom)
    }

    @Test
    fun testFromHexWithInvalidStrings() {
        val emptyString = factory.fromHex("")
        assertFalse(emptyString.isSampled)
        assertFalse(emptyString.isRandom)

        val shortString = factory.fromHex("1")
        assertFalse(shortString.isSampled)
        assertFalse(shortString.isRandom)

        val notHex = factory.fromHex("2g")
        assertFalse(notHex.isSampled)
        assertFalse(notHex.isRandom)
    }
}
