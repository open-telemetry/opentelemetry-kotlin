package io.opentelemetry.kotlin.logging

/**
 * Internal implementation of [LoggerConfig].
 */
internal class LoggerConfigImpl(
    override val enabled: Boolean = true,
    override val minimumSeverity: SeverityNumber = SeverityNumber.UNKNOWN,
    override val traceBased: Boolean = false,
) : LoggerConfig
