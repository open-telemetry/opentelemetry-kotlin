package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.resource.Resource

/**
 * One OTLP metric stream produced by an instrumentation scope and resource.
 *
 * A stream is identified by its resource, instrumentation scope, name, point kind, unit, and
 * intrinsic point properties such as temporality and monotonicity. Description is intentionally
 * non-identifying.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#opentelemetry-protocol-data-model
 */
@ExperimentalApi
@ThreadSafe
public interface MetricData {
    public val resource: Resource
    public val instrumentationScopeInfo: InstrumentationScopeInfo
    public val name: String
    public val description: String?
    public val unit: String?
    public val data: Data<PointData>
    public val isEmpty: Boolean
        get() = data.points.isEmpty()
}
