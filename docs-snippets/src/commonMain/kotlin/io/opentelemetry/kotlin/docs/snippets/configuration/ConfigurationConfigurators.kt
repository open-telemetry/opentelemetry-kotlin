package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.LoggerConfig
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.tracing.TracerConfig

// region configuration-configurators
private val disabledTracer = object : TracerConfig {
    override val enabled: Boolean = false
}
private val enabledTracer = object : TracerConfig {
    override val enabled: Boolean = true
}
private val warningsOnly = object : LoggerConfig {
    override val minimumSeverity: SeverityNumber = SeverityNumber.WARN
}
private val defaultLogger = object : LoggerConfig {}

fun initWithConfigurators(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        // Turn off one library's spans, matched by its instrumentation scope name.
        tracerConfigurator { scope ->
            when (scope.name) {
                "com.example.noisy" -> disabledTracer
                else -> enabledTracer
            }
        }
    }
    loggerProvider {
        loggerConfigurator { scope ->
            when {
                scope.name.startsWith("com.example.network") -> warningsOnly
                else -> defaultLogger
            }
        }
    }
}
// end-region
