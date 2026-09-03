package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class UpDownCounterImplTest {

    private lateinit var meter: Meter

    /** Mirrors the private MeterImpl.MAX_INSTRUMENT_NAME_CHARS constant. */
    private val maxNameChars = 255

    /** Mirrors the private MeterImpl.MAX_INSTRUMENT_UNIT_CHARS constant. */
    private val maxUnitChars = 63

    /** Mirrors the private MeterImpl.MAX_INSTRUMENT_DESCRIPTION_CHARS constant. */
    private val maxDescriptionChars = 1023

    @BeforeTest
    fun setup() {
        meter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = NoopSdkErrorHandler,
            )
        ).getMeter("test")
    }

    @Test
    fun createLongUpDownCounterExposesIdentity() {
        val counter = meter.createLongUpDownCounter(
            name = "store.inventory",
            unit = "{item}",
            description = "items in stock",
        )
        assertEquals("store.inventory", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("items in stock", counter.description)
        assertTrue(counter.enabled())
    }

    @Test
    fun createDoubleUpDownCounterExposesIdentity() {
        val counter = meter.createDoubleUpDownCounter(
            name = "queue.depth",
            unit = "{item}",
            description = "queue size",
        )
        assertEquals("queue.depth", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("queue size", counter.description)
        assertTrue(counter.enabled())
    }

    @Test
    fun longAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createLongUpDownCounter("grocery.customers")
        counter.add(1)
        counter.add(-1)
        counter.add(0)
        counter.add(2) { setStringAttribute("account.type", "commercial") }
    }

    @Test
    fun doubleAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createDoubleUpDownCounter("grocery.customers")
        counter.add(1.5)
        counter.add(-1.5)
        counter.add(0.0)
        counter.add(2.0) { setStringAttribute("account.type", "residential") }
    }

    @Test
    fun createUsesNullUnitAndDescriptionByDefault() {
        val longCounter = meter.createLongUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", longCounter.name)
        assertEquals(null, longCounter.unit)
        assertEquals(null, longCounter.description)

        val doubleCounter = meter.createDoubleUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", doubleCounter.name)
        assertEquals(null, doubleCounter.unit)
        assertEquals(null, doubleCounter.description)
    }

    @Test
    fun createWithInvalidNameReturnsNoopAndReportsApiMisuse() {
        val handler = FakeSdkErrorHandler()
        val invalidMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val counter = invalidMeter.createDoubleUpDownCounter("1 invalid name")

        assertEquals("1 invalid name", counter.name)
        assertFalse(counter.enabled())
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("Instrument.name", handler.apiMisuses.single().api)
    }

    @Test
    fun createAcceptsValidNamesWithoutError() {
        val handler = FakeSdkErrorHandler()
        val validMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val validNames = listOf(
            "a",
            "queue.depth",
            "http/server/duration",
            "A-b_c.d/e1",
            "a".repeat(maxNameChars),
        )
        for (name in validNames) {
            val counter = validMeter.createDoubleUpDownCounter(name)
            assertTrue(counter.enabled(), "expected \"$name\" to be valid")
        }
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun createRejectsInvalidNamesAndReportsApiMisuse() {
        val invalidNames = listOf(
            "",
            "1abc",
            "_foo",
            "/foo",
            "foo bar",
            "foo\$",
            "a".repeat(maxNameChars + 1),
            "café",
        )
        for (name in invalidNames) {
            val handler = FakeSdkErrorHandler()
            val invalidMeter = MeterProviderImpl(
                MetricsConfig(
                    resource = ResourceImpl(AttributesModel(), null),
                    sdkErrorHandler = handler,
                )
            ).getMeter("test")

            val counter = invalidMeter.createDoubleUpDownCounter(name)

            assertFalse(counter.enabled(), "expected \"$name\" to be invalid")
            assertEquals(1, handler.apiMisuses.size)
            assertEquals("Instrument.name", handler.apiMisuses.single().api)
        }
    }

    @Test
    fun createWithOverLongUnitDropsUnitAndReportsApiMisuse() {
        val handler = FakeSdkErrorHandler()
        val invalidUnitMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val unit = "a".repeat(maxUnitChars + 1)
        val counter = invalidUnitMeter.createDoubleUpDownCounter("grocery.customers", unit = unit)

        assertNull(counter.unit)
        assertTrue(counter.enabled())
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("Instrument.unit", handler.apiMisuses.single().api)
    }

    @Test
    fun createKeepsUnitAtLimitUnchanged() {
        val handler = FakeSdkErrorHandler()
        val limitMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val unit = "a".repeat(maxUnitChars)
        val counter = limitMeter.createDoubleUpDownCounter("grocery.customers", unit = unit)

        assertEquals(unit, counter.unit)
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun createWithNonAsciiUnitDropsUnitAndReportsApiMisuse() {
        val handler = FakeSdkErrorHandler()
        val nonAsciiUnitMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val counter = nonAsciiUnitMeter.createDoubleUpDownCounter("grocery.customers", unit = "café")

        assertNull(counter.unit)
        assertTrue(counter.enabled())
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("Instrument.unit", handler.apiMisuses.single().api)
    }

    @Test
    fun createTruncatesOverLongDescriptionAndReportsApiMisuse() {
        val handler = FakeSdkErrorHandler()
        val overLongMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val description = "a".repeat(maxDescriptionChars + 1)
        val counter = overLongMeter.createDoubleUpDownCounter("grocery.customers", description = description)

        assertEquals("a".repeat(maxDescriptionChars), counter.description)
        assertEquals(1, handler.apiMisuses.size)
    }

    @Test
    fun createKeepsDescriptionAtLimitUnchanged() {
        val handler = FakeSdkErrorHandler()
        val limitMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val description = "a".repeat(maxDescriptionChars)
        val counter = limitMeter.createDoubleUpDownCounter("grocery.customers", description = description)

        assertEquals(description, counter.description)
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun createKeepsSupplementaryPlaneCharacterWithinLimit() {
        val handler = FakeSdkErrorHandler()
        val supplementaryMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        // U+1F600 GRINNING FACE is a surrogate pair, one code point but two UTF-16 chars.
        val emoji = "\uD83D\uDE00"
        val description = emoji + "a".repeat(maxDescriptionChars - 1)
        val counter = supplementaryMeter.createDoubleUpDownCounter("grocery.customers", description = description)

        assertEquals(description, counter.description)
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun createTruncationDoesNotSplitSupplementaryPlaneCharacter() {
        val handler = FakeSdkErrorHandler()
        val supplementaryMeter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            )
        ).getMeter("test")

        val emoji = "\uD83D\uDE00"
        // Surrogate pair sits right at the boundary; truncation must drop it whole, not split it.
        val description = "a".repeat(maxDescriptionChars) + emoji
        val counter = supplementaryMeter.createDoubleUpDownCounter("grocery.customers", description = description)

        assertEquals("a".repeat(maxDescriptionChars), counter.description)
        assertEquals(1, handler.apiMisuses.size)
    }
}
