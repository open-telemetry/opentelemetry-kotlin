package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.tracing.data.StatusData

internal fun OtelJavaStatusData.toOtelKotlinStatusData(): StatusData = when (statusCode) {
    OtelJavaStatusCode.UNSET -> StatusData.Unset
    OtelJavaStatusCode.OK -> StatusData.Ok
    OtelJavaStatusCode.ERROR -> StatusData.Error(description)
    null -> StatusData.Unset
}
