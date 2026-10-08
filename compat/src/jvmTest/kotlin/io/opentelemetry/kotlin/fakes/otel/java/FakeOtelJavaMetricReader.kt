package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.CollectionRegistration
import io.opentelemetry.sdk.metrics.export.MetricReader

internal class FakeOtelJavaMetricReader : MetricReader {

    var flushCount: Int = 0
    var shutdownCount: Int = 0
    private var registration: CollectionRegistration? = null

    var nextResult: () -> OtelJavaCompletableResultCode = { OtelJavaCompletableResultCode.ofSuccess() }

    fun collect(): Collection<MetricData> = registration?.collectAllMetrics().orEmpty()

    override fun register(registration: CollectionRegistration) {
        this.registration = registration
    }

    override fun getAggregationTemporality(
        instrumentType: InstrumentType
    ): AggregationTemporality = AggregationTemporality.CUMULATIVE

    override fun forceFlush(): OtelJavaCompletableResultCode {
        flushCount += 1
        return nextResult()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return nextResult()
    }
}
