package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.tracing.data.StatusData

@OptIn(ExperimentalApi::class)
internal fun OtelJavaStatusCode.toOtelKotlinStatusData(description: String?): StatusData = when (this) {
    OtelJavaStatusCode.UNSET -> StatusData.Unset
    OtelJavaStatusCode.OK -> StatusData.Ok
    OtelJavaStatusCode.ERROR -> StatusData.Error(description)
}
