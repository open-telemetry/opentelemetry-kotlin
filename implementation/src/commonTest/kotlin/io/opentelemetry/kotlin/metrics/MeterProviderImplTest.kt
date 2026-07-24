package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class MeterProviderImplTest {

    private val metricsConfig = MetricsConfig(
        resource = ResourceImpl(AttributesModel(), null),
        sdkErrorHandler = NoopSdkErrorHandler,
    )
    private val contextFactory = ContextFactoryImpl(FakeSpanFactory())
    private lateinit var impl: MeterProviderImpl

    @BeforeTest
    fun setup() {
        impl = MeterProviderImpl(metricsConfig, contextFactory)
    }

    @Test
    fun testMinimalMeterProvider() {
        assertNotNull(impl.getMeter(name = ""))
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
}
