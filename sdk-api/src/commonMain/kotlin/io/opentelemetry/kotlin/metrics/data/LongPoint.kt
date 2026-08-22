package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

@ExperimentalApi
@ThreadSafe
public interface LongPoint : PointData {
    public val value: Long
}
