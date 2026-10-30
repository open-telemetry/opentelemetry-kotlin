package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

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
}
