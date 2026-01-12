package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertEquals("01", flags.hex)
    }

    @Test
    fun testSampledOnly() {
        val flags = factory.create(sampled = true, random = false)

        assertTrue(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("01", flags.hex)
    }

    @Test
    fun testRandomOnly() {
        val flags = factory.create(sampled = false, random = true)

        assertFalse(flags.isSampled)
        assertTrue(flags.isRandom)
        assertEquals("02", flags.hex)
    }

    @Test
    fun testSampledAndRandom() {
        val flags = factory.create(sampled = true, random = true)

        assertTrue(flags.isSampled)
        assertTrue(flags.isRandom)
        assertEquals("03", flags.hex)
    }

    @Test
    fun testDefault() {
        val flags = factory.create(sampled = false, random = false)

        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("00", flags.hex)
    }

    @Test
    fun testFromHexWithValidStrings() {
        val default = factory.fromHex("00")
        assertFalse(default.isSampled)
        assertFalse(default.isRandom)
        assertEquals("00", default.hex)

        val sampled = factory.fromHex("01")
        assertTrue(sampled.isSampled)
        assertFalse(sampled.isRandom)
        assertEquals("01", sampled.hex)

        val random = factory.fromHex("02")
        assertFalse(random.isSampled)
        assertTrue(random.isRandom)
        assertEquals("02", random.hex)

        val both = factory.fromHex("03")
        assertTrue(both.isSampled)
        assertTrue(both.isRandom)
        assertEquals("03", both.hex)

        val anotherBoth = factory.fromHex("2f") // 00101111
        assertTrue(anotherBoth.isSampled)
        assertTrue(anotherBoth.isRandom)
        assertEquals("03", anotherBoth.hex)
    }

    @Test
    fun testFromHexWithInvalidStrings() {
        val emptyString = factory.fromHex("")
        assertFalse(emptyString.isSampled)
        assertFalse(emptyString.isRandom)
        assertEquals("00", emptyString.hex)

        val shortString = factory.fromHex("1")
        assertFalse(shortString.isSampled)
        assertFalse(shortString.isRandom)
        assertEquals("00", shortString.hex)

        val notHex = factory.fromHex("2g")
        assertFalse(notHex.isSampled)
        assertFalse(notHex.isRandom)
        assertEquals("00", notHex.hex)
    }
}
