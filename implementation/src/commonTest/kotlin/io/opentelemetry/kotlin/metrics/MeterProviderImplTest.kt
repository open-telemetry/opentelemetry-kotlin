package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.metrics.instrument.InstrumentValueType
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class MeterProviderImplTest {

    private val metricsConfig = MetricsConfig(
        resource = ResourceImpl(AttributesModel(), null),
        sdkErrorHandler = NoopSdkErrorHandler,
    )
    private val attributeLimits = AttributeLimitsBehavior()
    private lateinit var impl: MeterProviderImpl

    @BeforeTest
    fun setup() {
        impl = MeterProviderImpl(metricsConfig, attributeLimits)
    }

    @Test
    fun testMinimalMeterProvider() {
        assertNotNull(impl.getMeter(name = ""))
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testObservableLongCounterIsNoopUntilMetricsCollectionIsImplemented() {
        var called = false
        val counter = impl.getMeter("test").createObservableLongCounter(
            name = "process.page.faults",
            callbacks = arrayOf({ called = true }),
        )

        assertEquals("process.page.faults", counter.name)
        counter.registerCallback { called = true }.close()
        counter.close()
        assertFalse(called)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testObservableLongUpDownCounterIsNoopUntilMetricsCollectionIsImplemented() {
        var called = false
        val counter = impl.getMeter("test").createObservableLongUpDownCounter(
            name = "process.memory.working_set",
            callbacks = arrayOf({ called = true }),
        )

        assertEquals("process.memory.working_set", counter.name)
        counter.registerCallback { called = true }.close()
        counter.close()
        assertFalse(called)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testObservableDoubleUpDownCounterIsNoopUntilMetricsCollectionIsImplemented() {
        var called = false
        val counter = impl.getMeter("test").createObservableDoubleUpDownCounter(
            name = "process.cpu.load",
            callbacks = arrayOf<(ObservableDoubleMeasurement) -> Unit>({ called = true }),
        )

        assertEquals("process.cpu.load", counter.name)
        counter.registerCallback { called = true }.close()
        counter.close()
        assertFalse(called)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testObservableLongGaugeIsNoopUntilMetricsCollectionIsImplemented() {
        var called = false
        val gauge = impl.getMeter("test").createObservableLongGauge(
            name = "queue.depth",
            callbacks = arrayOf({ called = true }),
        )

        assertEquals("queue.depth", gauge.name)
        gauge.registerCallback { called = true }.close()
        gauge.close()
        assertFalse(called)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testObservableDoubleGaugeIsNoopUntilMetricsCollectionIsImplemented() {
        var called = false
        val gauge = impl.getMeter("test").createObservableDoubleGauge(
            name = "temperature",
            callbacks = arrayOf<(ObservableDoubleMeasurement) -> Unit>({ called = true }),
        )

        assertEquals("temperature", gauge.name)
        gauge.registerCallback { called = true }.close()
        gauge.close()
        assertFalse(called)
    }

    @Test
    fun testLongGaugeExposesMetadata() {
        val gauge = impl.getMeter("test").createLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "Current queue depth",
        )

        assertEquals("queue.depth", gauge.name)
        assertEquals("{item}", gauge.unit)
        assertEquals("Current queue depth", gauge.description)
        assertTrue(gauge.enabled())
        gauge.record(10) { setStringAttribute("queue", "primary") }
    }

    @Test
    fun testInvalidLongGaugeNameReturnsNoop() {
        val handler = FakeSdkErrorHandler()
        val provider = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            ),
            attributeLimits,
        )

        val gauge = provider.getMeter("test").createLongGauge("1.invalid")

        assertFalse(gauge.enabled())
        assertEquals("Instrument.name", handler.apiMisuses.single().api)
    }

    @Test
    fun testDoubleGaugeExposesMetadata() {
        val gauge = impl.getMeter("test").createDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "Current temperature",
        )

        assertEquals("temperature", gauge.name)
        assertEquals("Cel", gauge.unit)
        assertEquals("Current temperature", gauge.description)
        assertTrue(gauge.enabled())
        gauge.record(21.5) { setStringAttribute("room", "server") }
    }

    @Test
    fun testInvalidDoubleGaugeNameReturnsNoop() {
        val handler = FakeSdkErrorHandler()
        val provider = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = handler,
            ),
            attributeLimits,
        )

        val gauge = provider.getMeter("test").createDoubleGauge("1.invalid")

        assertFalse(gauge.enabled())
        assertEquals("Instrument.name", handler.apiMisuses.single().api)
    }

    @Test
    fun testEmptyMeterNameReportsApiMisuse() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val attributeLimits = AttributeLimitsBehavior()
        val provider = MeterProviderImpl(config, attributeLimits)
        provider.getMeter(name = "")
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("MeterProvider.getMeter", handler.apiMisuses.single().api)
    }

    @Test
    fun testDupeMeterProviderName() {
        val first = impl.getMeter(name = "name")
        val second = impl.getMeter(name = "name")
        val third = impl.getMeter(name = "other")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeMeterProviderVersion() {
        val first = impl.getMeter(name = "name", version = "0.1.0")
        val second = impl.getMeter(name = "name", version = "0.1.0")
        val third = impl.getMeter(name = "name", version = "0.2.0")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testForceFlushNoReaders() = runTest {
        assertEquals(OperationResultCode.Success, impl.forceFlush())
    }

    @Test
    fun testShutdownNoReaders() = runTest {
        assertEquals(OperationResultCode.Success, impl.shutdown())
    }

    @Test
    fun testGetMeterAfterShutdownReturnsNoopMeter() = runTest {
        impl.shutdown()
        assertSame(NoopOpenTelemetry.meterProvider.getMeter(""), impl.getMeter("test"))
    }

    @Test
    fun testThrowingAttributesReturnsNoopMeter() {
        val errorHandler = FakeSdkErrorHandler()
        val provider = MeterProviderImpl(
            MetricsConfig(resource = ResourceImpl(AttributesModel(), null), sdkErrorHandler = errorHandler),
            attributeLimits = AttributeLimitsBehavior()
        )

        val meter = provider.getMeter(name = "name") { error("boom") }

        assertSame(NoopOpenTelemetry.meterProvider.getMeter(""), meter)
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("MeterProvider.getMeter failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }

    @Test
    fun testDifferentInstrumentsWithDifferentNames() {
        val meter = impl.getMeter(name = "test")
        val counter1 = meter.createLongUpDownCounter("counter1", unit = "1", description = "test")
        val counter2 = meter.createLongUpDownCounter("counter2", unit = "1", description = "test")
        assertNotEquals(counter1, counter2)
    }

    @Test
    fun testDuplicateDoubleUpDownCounterRegistrationReturnsSameInstance() {
        val meter = impl.getMeter(name = "test")
        val counter1 = meter.createDoubleUpDownCounter("my.counter", unit = "1", description = "test")
        val counter2 = meter.createDoubleUpDownCounter("my.counter", unit = "1", description = "test")
        assertSame(counter1, counter2)
    }

    @Test
    fun testInvalidInstrumentNameReturnsNoopInstrument() {
        val handler = FakeSdkErrorHandler()
        val config = MetricsConfig(
            resource = ResourceImpl(AttributesModel(), null),
            sdkErrorHandler = handler,
        )
        val attributeLimits = AttributeLimitsBehavior()
        val provider = MeterProviderImpl(config, attributeLimits)
        val meter = provider.getMeter(name = "test")

        val counter = meter.createLongUpDownCounter("invalid-name!", unit = "1", description = "test")

        // Should return a noop instrument
        assertFalse(counter.enabled())
        assertEquals(1, handler.apiMisuses.size)
        assertTrue(handler.apiMisuses.single().message.contains("invalid"))
    }

    @Test
    fun testNullUnitPreserved() {
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = null,
            description = "test",
        )
        assertEquals(null, gauge.unit)
    }

    @Test
    fun testEmptyUnitPreserved() {
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = "",
            description = "test",
        )
        assertEquals("", gauge.unit)
    }

    @Test
    fun testNonAsciiUnitPreserved() {
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = "°C",
            description = "test",
        )
        assertEquals("°C", gauge.unit)
    }

    @Test
    fun testLongUnitPreserved() {
        val longUnit = "a".repeat(100)
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = longUnit,
            description = "test",
        )
        assertEquals(longUnit, gauge.unit)
    }

    @Test
    fun testNullDescriptionPreserved() {
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = "1",
            description = null,
        )
        assertEquals(null, gauge.description)
    }

    @Test
    fun testEmptyDescriptionPreserved() {
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = "1",
            description = "",
        )
        assertEquals("", gauge.description)
    }

    @Test
    fun testLongDescriptionPreserved() {
        val longDesc = "a".repeat(2000)
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = "1",
            description = longDesc,
        )
        assertEquals(longDesc, gauge.description)
    }

    @Test
    fun testUnicodeDescriptionPreserved() {
        val unicodeDesc = "°"
        val gauge = impl.getMeter("test").createLongGauge(
            name = "test.counter",
            unit = "1",
            description = unicodeDesc,
        )
        assertEquals(unicodeDesc, gauge.description)
    }

    @Test
    fun testNullEmptyUnitDescriptorEquivalence() {
        val desc1 = InstrumentDescriptor(
            name = "test",
            unit = null,
            description = "desc",
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.LONG,
        )
        val desc2 = InstrumentDescriptor(
            name = "test",
            unit = "",
            description = "desc",
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.LONG,
        )
        assertEquals(desc1, desc2)
        assertEquals(desc1.hashCode(), desc2.hashCode())
    }

    @Test
    fun testNullEmptyDescriptionDescriptorEquivalence() {
        val desc1 = InstrumentDescriptor(
            name = "test",
            unit = "1",
            description = null,
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.LONG,
        )
        val desc2 = InstrumentDescriptor(
            name = "test",
            unit = "1",
            description = "",
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.LONG,
        )
        assertEquals(desc1, desc2)
        assertEquals(desc1.hashCode(), desc2.hashCode())
    }
}
