package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import io.opentelemetry.kotlin.framework.SdkTracker
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalApi::class)
internal class OtelJavaOpenTelemetryExtTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun testToOtelKotlinApi() {
        val otel = OtelJavaOpenTelemetry.noop().toOtelKotlinApi().also(sdks::track)
        checkNotNull(otel)
    }

    @Test
    fun testLifecycleOperationsReachAllJavaSdkProviders() = runTest {
        val fixture = createJavaSdkFixture()
        val closeable = assertIs<TelemetryCloseable>(fixture.sdk.toOtelKotlinApi().also(sdks::track))

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
        val closeable = assertIs<TelemetryCloseable>(fixture.sdk.toOtelKotlinApi().also(sdks::track))

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
        val closeable = assertIs<TelemetryCloseable>(fixture.sdk.toOtelKotlinApi().also(sdks::track))

        assertEquals(OperationResultCode.Failure, closeable.shutdown())
        assertEquals(1, fixture.spanProcessor.shutdownCount)
        assertEquals(1, fixture.logRecordProcessor.shutdownCount)
        assertEquals(1, fixture.metricReader.shutdownCount)
    }

    @Test
    fun testNoopJavaApiReportsFailureForUnsupportedLifecycleOperations() = runTest {
        val closeable = assertIs<TelemetryCloseable>(
            OtelJavaOpenTelemetry.noop().toOtelKotlinApi().also(sdks::track)
        )

        assertEquals(OperationResultCode.Failure, closeable.forceFlush())
        assertEquals(OperationResultCode.Failure, closeable.shutdown())
    }

    @Test
    fun testLifecycleExceptionsAreReportedToSuppliedErrorHandler() = runTest {
        val fixture = createJavaSdkFixture()
        fixture.spanProcessor.nextResult = { throw IllegalStateException("boom") }
        val errorHandler = FakeSdkErrorHandler()
        val closeable = assertIs<TelemetryCloseable>(
            fixture.sdk.toOtelKotlinApi(sdkErrorHandler = errorHandler).also(sdks::track)
        )

        assertEquals(OperationResultCode.Failure, closeable.forceFlush())
        assertEquals("TracerProvider.forceFlush failed", errorHandler.userCodeErrors.single().message)
    }

    @Test
    fun testThrowingErrorHandlerDoesNotPropagate() = runTest {
        val fixture = createJavaSdkFixture()
        fixture.spanProcessor.nextResult = { throw IllegalStateException("boom") }
        val closeable = assertIs<TelemetryCloseable>(
            fixture.sdk.toOtelKotlinApi(sdkErrorHandler = { error("handler boom") }).also(sdks::track)
        )

        assertEquals(OperationResultCode.Failure, closeable.forceFlush())
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
