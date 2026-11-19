package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.SeverityNumber

// region configuration-diagnostics
fun initWithDiagnostics(isDebugBuild: Boolean, userOptedOut: Boolean): OpenTelemetrySdk = createOpenTelemetry {
    // Receives errors and misuse detected by the SDK. The default writes to the platform log.
    errorHandler { error -> println("otel: $error") }

    // The lowest severity passed to the handler. Defaults to INFO.
    logLevel(
        when {
            isDebugBuild -> SeverityNumber.DEBUG
            else -> SeverityNumber.WARN
        }
    )

    // When true, createOpenTelemetry returns a no-op SDK.
    disabled(userOptedOut)
}
// end-region
