package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.tracing.data.StatusData

internal fun StatusData.toOtelJavaStatusData(): OtelJavaStatusData = when (this) {
    StatusData.Unset -> OtelJavaStatusData.unset()
    StatusData.Ok -> OtelJavaStatusData.ok()
    is StatusData.Error -> OtelJavaStatusData.create(OtelJavaStatusCode.ERROR, description)
}
