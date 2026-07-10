@file:OptIn(io.opentelemetry.kotlin.ExperimentalApi::class)

package io.opentelemetry.kotlin.minversion.app

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.OpenTelemetrySdk

// Forces a compile + dex (D8) against the published artifacts at the minimum toolchain.
internal class App {
    val api: OpenTelemetry? = null
    val sdk: OpenTelemetrySdk? = null
}
