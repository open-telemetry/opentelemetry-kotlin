package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalApi::class)
internal class OtelJavaOpenTelemetryExtTest {

    @Test
    fun testToOtelKotlinApi() {
        val otel = OtelJavaOpenTelemetry.noop().toOtelKotlinApi()
        checkNotNull(otel)
    }

    @Test
    fun testLifecycleOperationsReachAllJavaSdkProviders() = runTest {
        val fixture = createJavaSdkFixture()
        val closeable = assertIs<TelemetryCloseable>(fixture.sdk.toOtelKotlinApi())

        assertEquals(OperationResultCode.Success, closeable.forceFlush())
        assertEquals(1, fixture.spanProcessor.flushCount)
        assertEquals(1, fixture.logRecordProcessor.flushCount)
        assertEquals(1, fixture.metricReader.flushCount)

        assertEquals(OperationResultCode.Success, closeable.shutdown())
        assertEquals(1, fixture.spanProcessor.shutdownCount)
        assertEquals(1, fixture.logRecordProcessor.shutdownCount)
        assertEquals(1, fixture.metricReader.shutdownCount)

        assertEquals(OperationResultCode.Success, closeable.shutdown())
        assertEquals(1, fixture.spanProcessor.shutdownCount)
        assertEquals(1, fixture.logRecordProcessor.shutdownCount)
        assertEquals(1, fixture.metricReader.shutdownCount)
    }

    @Test
    fun testForceFlushReportsFailureAndContinuesWithRemainingProviders() = runTest {
        val fixture = createJavaSdkFixture()
        fixture.spanProcessor.nextResult = { OtelJavaCompletableResultCode.ofFailure() }
        val closeable = assertIs<TelemetryCloseable>(fixture.sdk.toOtelKotlinApi())

        assertEquals(OperationResultCode.Failure, closeable.forceFlush())
        assertEquals(1, fixture.spanProcessor.flushCount)
        assertEquals(1, fixture.logRecordProcessor.flushCount)
        assertEquals(1, fixture.metricReader.flushCount)

        fixture.spanProcessor.nextResult = { OtelJavaCompletableResultCode.ofSuccess() }
        assertEquals(OperationResultCode.Success, closeable.shutdown())
    }

    @Test
    fun testShutdownReportsFailureAndContinuesWithRemainingProviders() = runTest {
        val fixture = createJavaSdkFixture()
        fixture.spanProcessor.nextResult = { OtelJavaCompletableResultCode.ofFailure() }
        val closeable = assertIs<TelemetryCloseable>(fixture.sdk.toOtelKotlinApi())

        assertEquals(OperationResultCode.Failure, closeable.shutdown())
        assertEquals(1, fixture.spanProcessor.shutdownCount)
        assertEquals(1, fixture.logRecordProcessor.shutdownCount)
        assertEquals(1, fixture.metricReader.shutdownCount)
    }

    @Test
    fun testNoopJavaApiHasSuccessfulNoOpLifecycleOperations() = runTest {
        val closeable = assertIs<TelemetryCloseable>(
            OtelJavaOpenTelemetry.noop().toOtelKotlinApi()
        )

        assertEquals(OperationResultCode.Success, closeable.forceFlush())
        assertEquals(OperationResultCode.Success, closeable.shutdown())
    }

    private fun createJavaSdkFixture(): JavaSdkFixture {
        val spanProcessor = FakeOtelJavaSpanProcessor()
        val logRecordProcessor = FakeOtelJavaLogRecordProcessor()
        val metricReader = FakeOtelJavaMetricReader()
        val tracerProvider = OtelJavaSdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .build()
        val loggerProvider = OtelJavaSdkLoggerProvider.builder()
            .addLogRecordProcessor(logRecordProcessor)
            .build()
        val meterProvider = OtelJavaSdkMeterProvider.builder()
            .registerMetricReader(metricReader)
            .build()
        val sdk = OtelJavaOpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setLoggerProvider(loggerProvider)
            .setMeterProvider(meterProvider)
            .build()
        return JavaSdkFixture(
            sdk = sdk,
            spanProcessor = spanProcessor,
            logRecordProcessor = logRecordProcessor,
            metricReader = metricReader,
        )
    }

    private class JavaSdkFixture(
        val sdk: OtelJavaOpenTelemetrySdk,
        val spanProcessor: FakeOtelJavaSpanProcessor,
        val logRecordProcessor: FakeOtelJavaLogRecordProcessor,
        val metricReader: FakeOtelJavaMetricReader,
    )
}
