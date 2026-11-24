package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.LoggerConfig
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-configurator
private val quietConfig = object : LoggerConfig {
    override val minimumSeverity = SeverityNumber.WARN
}
private val defaultConfig = object : LoggerConfig {}

fun initLoggingWithConfigurator(): OpenTelemetrySdk = createOpenTelemetry {
    loggerProvider {
        loggerConfigurator { scope ->
            when {
                scope.name.startsWith("com.thirdparty.") -> quietConfig
                else -> defaultConfig
            }
        }
    }
}
// end-region
