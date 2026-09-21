package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

@ExperimentalApi
@ThreadSafe
public interface DoublePoint : PointData {
    public val value: Double
}
