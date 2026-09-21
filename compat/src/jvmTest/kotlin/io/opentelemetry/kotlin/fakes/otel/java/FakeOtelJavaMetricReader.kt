package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.export.CollectionRegistration
import io.opentelemetry.sdk.metrics.export.MetricReader

internal class FakeOtelJavaMetricReader : MetricReader {

    var flushCount: Int = 0
    var shutdownCount: Int = 0

    override fun register(registration: CollectionRegistration) = Unit

    override fun getAggregationTemporality(
        instrumentType: InstrumentType
    ): AggregationTemporality = AggregationTemporality.CUMULATIVE

    override fun forceFlush(): OtelJavaCompletableResultCode {
        flushCount += 1
        return OtelJavaCompletableResultCode.ofSuccess()
    }

    override fun shutdown(): OtelJavaCompletableResultCode {
        shutdownCount += 1
        return OtelJavaCompletableResultCode.ofSuccess()
    }
}
