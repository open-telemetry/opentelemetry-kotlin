package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.tracing.data.StatusData

@OptIn(ExperimentalApi::class)
internal fun StatusData.toOtelJavaStatusData(): OtelJavaStatusData = when (this) {
    StatusData.Unset -> OtelJavaStatusData.unset()
    StatusData.Ok -> OtelJavaStatusData.ok()
    is StatusData.Error -> OtelJavaStatusData.create(OtelJavaStatusCode.ERROR, description)
}
