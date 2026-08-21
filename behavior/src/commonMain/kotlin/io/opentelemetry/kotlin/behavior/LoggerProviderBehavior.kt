package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior of logging.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#loggerprovider
 */
@ExperimentalApi
data class LoggerProviderBehavior(

    /**
     * Limits on log record data capture.
     */
    val logLimits: LogLimitsBehavior? = null,
) : Behavior<LoggerProviderBehavior> {

    override fun mergeWith(higher: LoggerProviderBehavior): LoggerProviderBehavior = copy(
        logLimits = mergeNode(logLimits, higher.logLimits),
    )
}
