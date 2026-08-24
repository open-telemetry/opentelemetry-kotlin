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
    fun doubleAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createDoubleUpDownCounter("grocery.customers")
        counter.add(1.5)
        counter.add(-1.5)
        counter.add(0.0)
        counter.add(2.0) { setStringAttribute("account.type", "residential") }
    }

    @Test
    fun createUsesNullUnitAndDescriptionByDefault() {
        val counter = meter.createDoubleUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
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

        val description = "a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS + 1)
        val counter = overLongMeter.createDoubleUpDownCounter("grocery.customers", description = description)

        assertEquals("a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS), counter.description)
        assertEquals(1, handler.apiMisuses.size)
    }
}
