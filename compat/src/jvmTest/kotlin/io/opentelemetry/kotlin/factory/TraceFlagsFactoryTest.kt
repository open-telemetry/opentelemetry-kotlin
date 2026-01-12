package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceFlagsFactoryTest {

    private val factory = createCompatSdkFactory().traceFlagsFactory

    @Test
    fun `default property`() {
        val flags = factory.default

        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("00", flags.hex)
    }

    @Test
    fun `create sampled only`() {
        val flags = factory.create(sampled = true, random = false)

        assertTrue(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("01", flags.hex)
    }

    @Test
    fun `create random only`() {
        val flags = factory.create(sampled = false, random = true)

        assertFalse(flags.isSampled)
        assertTrue(flags.isRandom)
        assertEquals("02", flags.hex)
    }

    @Test
    fun `create sampled and random`() {
        val flags = factory.create(sampled = true, random = true)

        assertTrue(flags.isSampled)
        assertTrue(flags.isRandom)
        assertEquals("03", flags.hex)
    }

    @Test
    fun `create default via function`() {
        val flags = factory.create(sampled = false, random = false)

        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
        assertEquals("00", flags.hex)
    }

    @Test
    fun `fromHex with valid hex strings`() {
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
    }

    @Test
    fun `fromHex with valid hex strings bigger than 03`() {
        val default = factory.fromHex("04")
        assertFalse(default.isSampled)
        assertFalse(default.isRandom)
        assertEquals("00", default.hex)

        val sampled = factory.fromHex("05")
        assertTrue(sampled.isSampled)
        assertFalse(sampled.isRandom)
        assertEquals("01", sampled.hex)

        val random = factory.fromHex("06")
        assertFalse(random.isSampled)
        assertTrue(random.isRandom)
        assertEquals("02", random.hex)

        val both = factory.fromHex("07")
        assertTrue(both.isSampled)
        assertTrue(both.isRandom)
        assertEquals("03", both.hex)
    }

    @Test
    fun `fromHex with invalid hex strings`() {
        val emptyString = factory.fromHex("")
        assertFalse(emptyString.isSampled)
        assertFalse(emptyString.isRandom)
        assertEquals("00", emptyString.hex)

        val shortString = factory.fromHex("1")
        assertFalse(shortString.isSampled)
        assertFalse(shortString.isRandom)
        assertEquals("00", shortString.hex)

        val invalidHex = factory.fromHex("zz")
        assertFalse(invalidHex.isSampled)
        assertFalse(invalidHex.isRandom)
        assertEquals("00", invalidHex.hex)
    }
}
