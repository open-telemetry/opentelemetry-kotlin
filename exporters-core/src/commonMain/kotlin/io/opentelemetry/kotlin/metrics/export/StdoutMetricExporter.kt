package io.opentelemetry.kotlin.metrics.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.metrics.data.AggregationTemporality
import io.opentelemetry.kotlin.metrics.data.DoublePoint
import io.opentelemetry.kotlin.metrics.data.GaugeData
import io.opentelemetry.kotlin.metrics.data.HistogramData
import io.opentelemetry.kotlin.metrics.data.LongPoint
import io.opentelemetry.kotlin.metrics.data.MetricData
import io.opentelemetry.kotlin.metrics.data.PointData
import io.opentelemetry.kotlin.metrics.data.SumData
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.platformLog

/**
 * A [MetricExporter] that outputs metric data to stdout.
 *
 * Note: This exporter is intended for debugging and learning purposes. It is not recommended
 * for production use. The output format is not standardized and can change at any time.
 */
internal class StdoutMetricExporter(
    private val sdkErrorHandler: SdkErrorHandler,
    private val logger: (String) -> Unit = ::platformLog,
    public val temporality: (InstrumentKind) -> AggregationTemporality = { AggregationTemporality.CUMULATIVE },
) : MetricExporter {

    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<MetricData>): OperationResultCode =
        shutdownState.ifActive {
            val failures = telemetry.count { metric ->
                !sdkErrorHandler.guardOrDefault(false, "Failed to write metric to stdout") {
                    logger(formatMetric(metric))
                    true
                }
            }
            if (failures == 0) {
                OperationResultCode.Success
            } else {
                OperationResultCode.Failure
            }
        }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            OperationResultCode.Success
        }

    /**
     * Formats a [MetricData] into a human-readable string representation.
     */
    internal fun formatMetric(metric: MetricData): String = buildString {
        append("Metric: ")
        append(metric.name)
        metric.unit?.let {
            append(" [")
            append(it)
            append("]")
        }
        appendLine()

        metric.description?.let {
            append("  Description: ")
            appendLine(it)
        }

        append("  Resource: ")
        appendLine(metric.resource)

        append("  InstrumentationScope: ")
        appendLine(metric.instrumentationScopeInfo.name)

        appendLine("  Data:")
        when (val data = metric.data) {
            is SumData<*> -> {
                append("    Type: Sum (monotonic=")
                append(data.isMonotonic)
                append(", temporality=")
                append(data.aggregationTemporality)
                appendLine(")")
                data.points.forEach { point ->
                    append("    Point: ")
                    appendPoint(point)
                    appendLine()
                }
            }
            is GaugeData<*> -> {
                appendLine("    Type: Gauge")
                data.points.forEach { point ->
                    append("    Point: ")
                    appendPoint(point)
                    appendLine()
                }
            }
            is HistogramData -> {
                append("    Type: Histogram (temporality=")
                append(data.aggregationTemporality)
                appendLine(")")
                data.points.forEach { point ->
                    append("    Point: ")
                    append("count=")
                    append(point.count)
                    append(", sum=")
                    append(point.sum)
                    if (point.attributes.isNotEmpty()) {
                        append(", attributes=")
                        append(point.attributes)
                    }
                    appendLine()
                }
            }
            else -> {
                append("    Type: ")
                appendLine(data::class.simpleName ?: "Unknown")
                data.points.forEach { point ->
                    append("    Point: ")
                    appendPoint(point)
                    appendLine()
                }
            }
        }
    }.trimEnd()

    private fun StringBuilder.appendPoint(point: PointData) {
        when (point) {
            is LongPoint -> append("value=").append(point.value)
            is DoublePoint -> append("value=").append(point.value)
            else -> append("point=").append(point)
        }
        if (point.attributes.isNotEmpty()) {
            append(", attributes=").append(point.attributes)
        }
        if (point.startEpochNanos > 0) {
            append(", startEpochNanos=").append(point.startEpochNanos)
        }
        append(", epochNanos=").append(point.epochNanos)
    }
}
