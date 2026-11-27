package io.opentelemetry.kotlin.metrics.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.FakeExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.metrics.data.AggregationTemporality
import io.opentelemetry.kotlin.metrics.data.Data
import io.opentelemetry.kotlin.metrics.data.LongPoint
import io.opentelemetry.kotlin.metrics.data.MetricData
import io.opentelemetry.kotlin.metrics.data.PointData
import io.opentelemetry.kotlin.metrics.data.SumData
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class StdoutMetricExporterTest {

    private val exportConfig = FakeExportConfig()

    @Test
    fun testExportMinimalMetric() = runTest {
        val output = mutableListOf<String>()
        val exporter = exportConfig.stdoutMetricExporter(output::add)

        val metric = FakeMetric(
            name = "test.metric",
            unit = "ms",
            description = "A test metric",
            data = FakeLongSumData(
                isMonotonic = true,
                aggregationTemporality = AggregationTemporality.CUMULATIVE,
                points = listOf(
                    FakeLongPoint(
                        value = 42L,
                        startEpochNanos = 1000L,
                        epochNanos = 2000L,
                    )
                )
            )
        )

        val result = exporter.export(listOf(metric))
        assertEquals(OperationResultCode.Success, result)
        assertEquals(1, output.size)

        val text = output.single()
        assertTrue(text.contains("Metric: test.metric [ms]"))
        assertTrue(text.contains("Description: A test metric"))
        assertTrue(text.contains("Type: Sum (monotonic=true, temporality=CUMULATIVE)"))
        assertTrue(text.contains("Point: value=42, startEpochNanos=1000, epochNanos=2000"))
    }

    @Test
    fun testDefaultTemporalityIsCumulative() {
        val exporter = exportConfig.stdoutMetricExporter() as StdoutMetricExporter
        assertEquals(AggregationTemporality.CUMULATIVE, exporter.temporality(InstrumentKind.HISTOGRAM))
    }

    @Test
    fun testCustomTemporality() {
        val exporter = exportConfig.stdoutMetricExporter(
            temporality = { AggregationTemporality.DELTA }
        ) as StdoutMetricExporter
        assertEquals(AggregationTemporality.DELTA, exporter.temporality(InstrumentKind.HISTOGRAM))
    }

    @Test
    fun testForceFlush() = runTest {
        val exporter = StdoutMetricExporter(NoopSdkErrorHandler)
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testShutdown() = runTest {
        val exporter = StdoutMetricExporter(NoopSdkErrorHandler)
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        val output = mutableListOf<String>()
        val exporter = StdoutMetricExporter(NoopSdkErrorHandler, output::add)
        exporter.shutdown()

        val metric = FakeMetric(name = "test.metric")
        val result = exporter.export(listOf(metric))
        assertEquals(OperationResultCode.Failure, result)
        assertEquals(0, output.size)
    }

    @Test
    fun testItemThatFailsToFormatDoesNotStopTheBatch() = runTest {
        val output = mutableListOf<String>()
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FakeExportConfig(sdkErrorHandler = errorHandler).stdoutMetricExporter(output::add)

        val throwingMetric = object : MetricData by FakeMetric(name = "throwing") {
            override val name: String get() = error("boom")
        }

        val result = exporter.export(listOf(
            FakeMetric(name = "first"),
            throwingMetric,
            FakeMetric(name = "third")
        ))

        assertEquals(OperationResultCode.Failure, result)
        assertEquals(2, output.size)
        assertTrue(output[0].startsWith("Metric: first"))
        assertTrue(output[1].startsWith("Metric: third"))
        assertEquals(1, errorHandler.errors.size)
        assertEquals(1, errorHandler.userCodeErrors.size)
    }

    private data class FakeMetric(
        override val name: String,
        override val description: String? = null,
        override val unit: String? = null,
        override val resource: Resource = FakeResource(emptyMap()),
        override val instrumentationScopeInfo: InstrumentationScopeInfo = FakeInstrumentationScopeInfo("test.scope"),
        override val data: Data<PointData> = FakeLongSumData(),
    ) : MetricData

    private data class FakeLongSumData(
        override val isMonotonic: Boolean = false,
        override val aggregationTemporality: AggregationTemporality = AggregationTemporality.CUMULATIVE,
        override val points: List<LongPoint> = emptyList(),
    ) : SumData.LongSumData

    private data class FakeLongPoint(
        override val value: Long = 0L,
        override val startEpochNanos: Long = 0L,
        override val epochNanos: Long = 0L,
        override val attributes: Map<String, Any> = emptyMap(),
    ) : LongPoint
}
