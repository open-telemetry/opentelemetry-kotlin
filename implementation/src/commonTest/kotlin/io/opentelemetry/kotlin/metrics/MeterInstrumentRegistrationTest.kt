package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class MeterInstrumentRegistrationTest {

    private val attributeLimits = AttributeLimitsBehavior()

    // Copilot Review Finding 1: Preserve conflicting instrument registrations
    @Test
    fun testSameNameDifferentUnitRetainsRequestedUnit() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter1 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("my.counter", unit = "ms", description = "test")

        // Should return the first instrument (spec says to reuse existing)
        assertSame(counter1, counter2)
        // Should report a warning about descriptor mismatch
        assertEquals(1, handler.apiMisuses.size)
        assertTrue(handler.apiMisuses.single().message.contains("different unit or description"))
        assertTrue(handler.apiMisuses.single().severity == SdkErrorSeverity.WARNING)
    }

    @Test
    fun testSameNameDifferentDescriptionRetainsRequestedDescription() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter1 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "first")
        val counter2 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "second")

        // Should return the first instrument (spec says to reuse existing)
        assertSame(counter1, counter2)
        // Should report a warning about descriptor mismatch
        assertEquals(1, handler.apiMisuses.size)
        assertTrue(handler.apiMisuses.single().message.contains("different unit or description"))
        assertTrue(handler.apiMisuses.single().severity == SdkErrorSeverity.WARNING)
    }

    @Test
    fun testLongAndDoubleInstrumentsWithSameNameBothFunctional() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val longCounter = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val doubleCounter = meter.createDoubleUpDownCounter("my.counter", unit = "1", description = "test")

        // Should not be the same instance (different valueTypes)
        assertFalse(longCounter === doubleCounter)
        // First should be functional, second should be noop (type mismatch)
        assertTrue(longCounter.enabled())
        assertFalse(doubleCounter.enabled())
        // Should report an error about type mismatch
        assertEquals(1, handler.apiMisuses.size)
        assertTrue(handler.apiMisuses.single().message.contains("already registered"))
        assertTrue(handler.apiMisuses.single().message.contains("LONG"))
        assertTrue(handler.apiMisuses.single().message.contains("DOUBLE"))
        assertTrue(handler.apiMisuses.single().severity == SdkErrorSeverity.ERROR)
    }

    @Test
    fun testIdenticalDescriptorsCorrectlyReuseExistingInstrument() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter1 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")

        // Should return the same instance (identical descriptors)
        assertSame(counter1, counter2)
        // Should not report any errors
        assertEquals(0, handler.apiMisuses.size)
    }

    // Copilot Review Finding 2: Report case-only instrument name conflicts
    @Test
    fun testCaseOnlyNameConflictReportsError() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter1 = meter.createLongUpDownCounter("My.Counter", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")

        // Should return the same instance (case-insensitive)
        assertSame(counter1, counter2)
        // Should report an error about case-only difference
        assertEquals(1, handler.apiMisuses.size)
        assertTrue(handler.apiMisuses.single().message.contains("differs only in case"))
        assertTrue(handler.apiMisuses.single().message.contains("My.Counter"))
        assertTrue(handler.apiMisuses.single().message.contains("my.counter"))
        assertTrue(handler.apiMisuses.single().severity == SdkErrorSeverity.ERROR)
    }

    @Test
    fun testCaseOnlyNameConflictReverseOrder() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter1 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("My.Counter", unit = "1", description = "test")

        // Should return the same instance (case-insensitive)
        assertSame(counter1, counter2)
        // Should report an error about case-only difference
        assertEquals(1, handler.apiMisuses.size)
        assertTrue(handler.apiMisuses.single().message.contains("differs only in case"))
    }

    @Test
    fun testIdenticalCaseDoesNotReportError() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter1 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("my.counter", unit = "1", description = "test")

        // Should return the same instance
        assertSame(counter1, counter2)
        // Should not report any errors (identical case)
        assertEquals(0, handler.apiMisuses.size)
    }
}
