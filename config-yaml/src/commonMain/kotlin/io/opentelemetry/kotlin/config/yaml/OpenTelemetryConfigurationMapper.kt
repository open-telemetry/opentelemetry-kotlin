package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SeverityLevel
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import io.opentelemetry.kotlin.config.schema.model.SeverityNumber
import io.opentelemetry.kotlin.config.yaml.logging.toBehavior
import io.opentelemetry.kotlin.config.yaml.tracing.toBehavior

/**
 * Maps a declarative config file onto the behavior it supplies, as one layer.
 */
@ExperimentalApi
fun OpenTelemetryConfiguration.toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
    fileFormat = fileFormat,
    disabled = disabled,
    logLevel = logLevel?.toSeverityLevel(),
    distribution = distribution,
    attributeLimits = attributeLimits?.toBehavior(),
    resource = resource?.toBehavior(),
    loggerProvider = loggerProvider?.toBehavior(),
    tracerProvider = tracerProvider?.toBehavior(),
)

@ExperimentalApi
internal fun SeverityNumber.toSeverityLevel(): SeverityLevel? = when (this) {
    SeverityNumber.TRACE -> SeverityLevel.TRACE
    SeverityNumber.TRACE2 -> SeverityLevel.TRACE2
    SeverityNumber.TRACE3 -> SeverityLevel.TRACE3
    SeverityNumber.TRACE4 -> SeverityLevel.TRACE4
    SeverityNumber.DEBUG -> SeverityLevel.DEBUG
    SeverityNumber.DEBUG2 -> SeverityLevel.DEBUG2
    SeverityNumber.DEBUG3 -> SeverityLevel.DEBUG3
    SeverityNumber.DEBUG4 -> SeverityLevel.DEBUG4
    SeverityNumber.INFO -> SeverityLevel.INFO
    SeverityNumber.INFO2 -> SeverityLevel.INFO2
    SeverityNumber.INFO3 -> SeverityLevel.INFO3
    SeverityNumber.INFO4 -> SeverityLevel.INFO4
    SeverityNumber.WARN -> SeverityLevel.WARN
    SeverityNumber.WARN2 -> SeverityLevel.WARN2
    SeverityNumber.WARN3 -> SeverityLevel.WARN3
    SeverityNumber.WARN4 -> SeverityLevel.WARN4
    SeverityNumber.ERROR -> SeverityLevel.ERROR
    SeverityNumber.ERROR2 -> SeverityLevel.ERROR2
    SeverityNumber.ERROR3 -> SeverityLevel.ERROR3
    SeverityNumber.ERROR4 -> SeverityLevel.ERROR4
    SeverityNumber.FATAL -> SeverityLevel.FATAL
    SeverityNumber.FATAL2 -> SeverityLevel.FATAL2
    SeverityNumber.FATAL3 -> SeverityLevel.FATAL3
    SeverityNumber.FATAL4 -> SeverityLevel.FATAL4
}
