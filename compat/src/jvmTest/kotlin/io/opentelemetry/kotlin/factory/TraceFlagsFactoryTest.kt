package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Test
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
    }

    @Test
    fun `create sampled only`() {
        val flags = factory.create(sampled = true, random = false)
        assertTrue(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun `create random only`() {
        val flags = factory.create(sampled = false, random = true)
        assertFalse(flags.isSampled)
        assertTrue(flags.isRandom)
    }

    @Test
    fun `create sampled and random`() {
        val flags = factory.create(sampled = true, random = true)
        assertTrue(flags.isSampled)
        assertTrue(flags.isRandom)
    }

    @Test
    fun `create default via function`() {
        val flags = factory.create(sampled = false, random = false)
        assertFalse(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun `fromHex with valid hex strings`() {
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
    }

    @Test
    fun `fromHex with valid hex strings bigger than 03`() {
        val default = factory.fromHex("04")
        assertFalse(default.isSampled)
        assertFalse(default.isRandom)

        val sampled = factory.fromHex("05")
        assertTrue(sampled.isSampled)
        assertFalse(sampled.isRandom)

        val random = factory.fromHex("06")
        assertFalse(random.isSampled)
        assertTrue(random.isRandom)

        val both = factory.fromHex("07")
        assertTrue(both.isSampled)
        assertTrue(both.isRandom)
    }

    @Test
    fun `fromHex with invalid hex strings`() {
        val emptyString = factory.fromHex("")
        assertFalse(emptyString.isSampled)
        assertFalse(emptyString.isRandom)

        val shortString = factory.fromHex("1")
        assertFalse(shortString.isSampled)
        assertFalse(shortString.isRandom)

        val invalidHex = factory.fromHex("zz")
        assertFalse(invalidHex.isSampled)
        assertFalse(invalidHex.isRandom)
    }
}
