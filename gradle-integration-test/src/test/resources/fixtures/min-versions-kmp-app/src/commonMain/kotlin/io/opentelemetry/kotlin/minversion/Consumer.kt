@file:OptIn(io.opentelemetry.kotlin.ExperimentalApi::class)

package io.opentelemetry.kotlin.minversion

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.OpenTelemetrySdk

internal class Consumer {
    val api: OpenTelemetry? = null
    val sdk: OpenTelemetrySdk? = null
}
