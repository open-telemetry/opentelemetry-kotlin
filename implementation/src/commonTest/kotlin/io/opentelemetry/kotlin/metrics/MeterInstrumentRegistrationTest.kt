package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class MeterInstrumentRegistrationTest {

    private val attributeLimits = AttributeLimitsBehavior()

    private fun newMeter(handler: FakeSdkErrorHandler): Meter {
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        return MeterProviderImpl(config, attributeLimits).getMeter(name = "test")
    }

    // Copilot Review Finding 1: Preserve conflicting instrument registrations
    @Test
    fun testSameNameDifferentUnitOrDescriptionCreatesSeparateInstruments() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val unit1 = meter.createLongUpDownCounter("unit.counter", unit = "1", description = "test")
        val unit2 = meter.createLongUpDownCounter("unit.counter", unit = "ms", description = "test")
        assertNotSame(unit1, unit2)
        assertTrue(unit1.enabled())
        assertTrue(unit2.enabled())
        assertEquals("1", unit1.unit)
        assertEquals("ms", unit2.unit)
        assertEquals("test", unit1.description)
        assertEquals("test", unit2.description)

        val desc1 = meter.createLongUpDownCounter("desc.counter", unit = "1", description = "first")
        val desc2 = meter.createLongUpDownCounter("desc.counter", unit = "1", description = "second")
        assertNotSame(desc1, desc2)
        assertTrue(desc1.enabled())
        assertTrue(desc2.enabled())
        assertEquals("first", desc1.description)
        assertEquals("second", desc2.description)

        assertEquals(2, handler.apiMisuses.size)
        handler.apiMisuses.forEach {
            assertTrue(it.message.contains("different unit or description"))
            assertTrue(it.severity == SdkErrorSeverity.WARNING)
        }

        // Identical descriptors still reuse the existing instance.
        assertSame(unit1, meter.createLongUpDownCounter("unit.counter", unit = "1", description = "test"))
        assertSame(desc1, meter.createLongUpDownCounter("desc.counter", unit = "1", description = "first"))
        assertEquals(2, handler.apiMisuses.size)
    }

    @Test
    fun testLongAndDoubleInstrumentsWithSameName() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val longCounter = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val doubleCounter = meter.createDoubleUpDownCounter("my.counter", unit = "1", description = "test")

        // Type mismatch: both registrations stay functional with their own types.
        assertTrue(longCounter !== doubleCounter)
        assertTrue(longCounter.enabled())
        assertTrue(doubleCounter.enabled())
        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertTrue(error.message.contains("already registered"))
        assertTrue(error.message.contains("LONG"))
        assertTrue(error.message.contains("DOUBLE"))
        assertTrue(error.severity == SdkErrorSeverity.ERROR)
    }

    // Copilot Review Finding: first-seen casing is preserved for conflicting registrations
    @Test
    fun testCaseVariantWithUnitOrDescriptionConflictKeepsFirstSeenName() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val first = meter.createLongUpDownCounter("My.Counter", unit = "1", description = "first")
        val unitVariant = meter.createLongUpDownCounter("my.counter", unit = "ms", description = "first")
        val descVariant = meter.createLongUpDownCounter("MY.COUNTER", unit = "1", description = "second")

        assertTrue(first !== unitVariant)
        assertTrue(first !== descVariant)
        assertTrue(unitVariant.enabled())
        assertTrue(descVariant.enabled())

        // All registrations in the bucket keep the first-seen name casing.
        assertEquals("My.Counter", first.name)
        assertEquals("My.Counter", unitVariant.name)
        assertEquals("My.Counter", descVariant.name)

        // Each registration retains its own requested properties.
        assertEquals("1", first.unit)
        assertEquals("ms", unitVariant.unit)
        assertEquals("first", first.description)
        assertEquals("second", descVariant.description)

        assertEquals(2, handler.apiMisuses.size)
        handler.apiMisuses.forEach {
            assertTrue(it.message.contains("differs only in case"))
            assertTrue(it.severity == SdkErrorSeverity.ERROR)
        }
    }

    @Test
    fun testCaseVariantWithTypeConflictKeepsFirstSeenName() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val longCounter = meter.createLongUpDownCounter("My.Counter", unit = "1", description = "test")
        val doubleCounter = meter.createDoubleUpDownCounter("my.counter", unit = "1", description = "test")

        assertTrue(longCounter !== doubleCounter)
        assertTrue(longCounter.enabled())
        assertTrue(doubleCounter.enabled())
        assertEquals("My.Counter", longCounter.name)
        assertEquals("My.Counter", doubleCounter.name)

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertTrue(error.message.contains("already registered"))
        assertTrue(error.severity == SdkErrorSeverity.ERROR)
    }

    @Test
    fun testIdenticalDescriptorsReuseExistingInstrumentWithoutError() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val counter1 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val double1 = meter.createDoubleUpDownCounter("dbl.counter", unit = "1", description = "test")
        val double2 = meter.createDoubleUpDownCounter("dbl.counter", unit = "1", description = "test")

        assertSame(counter1, counter2)
        assertSame(double1, double2)
        assertEquals(0, handler.apiMisuses.size)
    }

    // Copilot Review Finding 2: Report case-only instrument name conflicts
    @Test
    fun testCaseOnlyNameConflictReportsErrorInBothOrders() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)

        // Uppercase first, then lowercase.
        val meter1 = provider.getMeter(name = "test1")
        val upper = meter1.createLongUpDownCounter("My.Counter", unit = "1", description = "test")
        val lower = meter1.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        assertSame(upper, lower)

        // Lowercase first, then uppercase.
        val meter2 = provider.getMeter(name = "test2")
        val lowerFirst = meter2.createLongUpDownCounter("other.counter", unit = "1", description = "test")
        val upperSecond = meter2.createLongUpDownCounter("Other.Counter", unit = "1", description = "test")
        assertSame(lowerFirst, upperSecond)

        assertEquals(2, handler.apiMisuses.size)
        handler.apiMisuses.forEach { error ->
            assertTrue(error.message.contains("differs only in case"))
            assertTrue(error.severity == SdkErrorSeverity.ERROR)
        }
    }
}
