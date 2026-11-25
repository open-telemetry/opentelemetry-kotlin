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

    private fun newMeter(handler: FakeSdkErrorHandler): Meter =
        MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            ),
            attributeLimits,
        ).getMeter(name = "test")

    @Test
    fun testConflictingRegistrationsStayFunctionalAndReuseIdentical() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val unit1 = meter.createLongUpDownCounter("unit.counter", unit = "1", description = "test")
        val unit2 = meter.createLongUpDownCounter("unit.counter", unit = "ms", description = "test")
        assertNotSame(unit1, unit2)
        assertTrue(unit1.enabled() && unit2.enabled())
        assertEquals("1", unit1.unit)
        assertEquals("ms", unit2.unit)

        val desc1 = meter.createLongUpDownCounter("desc.counter", unit = "1", description = "first")
        val desc2 = meter.createLongUpDownCounter("desc.counter", unit = "1", description = "second")
        assertNotSame(desc1, desc2)
        assertTrue(desc1.enabled() && desc2.enabled())
        assertEquals("first", desc1.description)
        assertEquals("second", desc2.description)

        assertEquals(2, handler.apiMisuses.size)
        handler.apiMisuses.forEach {
            assertTrue(it.message.contains("different unit or description"))
            assertTrue(it.severity == SdkErrorSeverity.WARNING)
        }

        // Identical descriptors still reuse the existing instance without reporting.
        assertSame(unit1, meter.createLongUpDownCounter("unit.counter", unit = "1", description = "test"))
        assertSame(desc1, meter.createLongUpDownCounter("desc.counter", unit = "1", description = "first"))
        assertEquals(2, handler.apiMisuses.size)
    }

    @Test
    fun testNumericTypeConflictKeepsBothRegistrationsFunctional() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val longCounter = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val doubleCounter = meter.createDoubleUpDownCounter("my.counter", unit = "1", description = "test")

        assertTrue(longCounter !== doubleCounter)
        assertTrue(longCounter.enabled() && doubleCounter.enabled())

        val error = handler.apiMisuses.single()
        assertTrue(error.message.contains("already registered"))
        assertTrue(error.message.contains("LONG"))
        assertTrue(error.message.contains("DOUBLE"))
        assertTrue(error.severity == SdkErrorSeverity.ERROR)
    }

    @Test
    fun testCaseConflictsKeepFirstSeenNameAndRequestedProperties() {
        val handler = FakeSdkErrorHandler()
        val meter = newMeter(handler)

        val first = meter.createLongUpDownCounter("My.Counter", unit = "1", description = "first")
        val unitVariant = meter.createLongUpDownCounter("my.counter", unit = "ms", description = "first")
        val descVariant = meter.createLongUpDownCounter("MY.COUNTER", unit = "1", description = "second")
        val typeVariant = meter.createDoubleUpDownCounter("mY.cOuNtEr", unit = "1", description = "first")

        // Conflicting registrations are distinct but stay enabled.
        assertTrue(first !== unitVariant && first !== descVariant && first !== typeVariant)
        assertTrue(first.enabled() && unitVariant.enabled() && descVariant.enabled() && typeVariant.enabled())

        // Every registration in the bucket keeps the first-seen name casing.
        listOf(first, unitVariant, descVariant, typeVariant).forEach {
            assertEquals("My.Counter", it.name)
        }
        // Each retains its own requested unit, description, and numeric type.
        assertEquals("1", first.unit)
        assertEquals("ms", unitVariant.unit)
        assertEquals("first", first.description)
        assertEquals("second", descVariant.description)

        assertEquals(3, handler.apiMisuses.size)
        handler.apiMisuses.forEach {
            assertTrue(it.severity == SdkErrorSeverity.ERROR)
        }
        assertTrue(handler.apiMisuses.count { it.message.contains("differs only in case") } == 2)
        assertTrue(handler.apiMisuses.count { it.message.contains("already registered") } == 1)
    }

    @Test
    fun testCaseOnlyNameConflictReportsErrorInBothOrders() {
        val handler = FakeSdkErrorHandler()
        val provider = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            ),
            attributeLimits,
        )

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
