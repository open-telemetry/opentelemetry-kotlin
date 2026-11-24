package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.TracerConfig

// region tracing-configurator
private val disabledConfig = object : TracerConfig {
    override val enabled = false
}
private val defaultConfig = object : TracerConfig {
    override val enabled = true
}

fun initTracingWithConfigurator(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        tracerConfigurator { scope ->
            when {
                scope.name.startsWith("com.thirdparty.") -> disabledConfig
                else -> defaultConfig
            }
        }
    }
}
// end-region
