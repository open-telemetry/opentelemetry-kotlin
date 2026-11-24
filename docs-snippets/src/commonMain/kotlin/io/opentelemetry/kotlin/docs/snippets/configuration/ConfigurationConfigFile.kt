package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.config.yamlConfigReader
import io.opentelemetry.kotlin.createOpenTelemetry

// region configuration-config-file
fun initFromConfigFile(): OpenTelemetrySdk = createOpenTelemetry {
    configFile(yamlConfigReader()) // reads the path in OTEL_CONFIG_FILE
}

fun initFromExplicitConfigFile(): OpenTelemetrySdk = createOpenTelemetry {
    configFile(yamlConfigReader(), path = "/etc/otel/otel.yaml")
}
// end-region
