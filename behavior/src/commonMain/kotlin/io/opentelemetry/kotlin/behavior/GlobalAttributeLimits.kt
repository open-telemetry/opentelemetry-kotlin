package io.opentelemetry.kotlin.behavior

/**
 * Folds the global attribute limits into each signal's limits, so a consumer can read a signal's
 * node on its own rather than repeating the fallback.
 *
 * A limit configured on the signal wins, and the global limit fills the gaps.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#attribute-limits
 */
internal fun OpenTelemetryBehavior.applyGlobalAttributeLimits(): OpenTelemetryBehavior {
    val global = attributeLimits ?: return this
    val tracer = tracerProvider ?: TracerProviderBehavior()
    val logger = loggerProvider ?: LoggerProviderBehavior()
    return copy(
        tracerProvider = tracer.copy(spanLimits = global.mergeInto(tracer.spanLimits)),
        loggerProvider = logger.copy(logLimits = global.mergeWith(logger.logLimits ?: LogLimitsBehavior())),
    )
}

private fun AttributeLimitsBehavior.mergeInto(spanLimits: SpanLimitsBehavior?): SpanLimitsBehavior {
    val limits = spanLimits ?: SpanLimitsBehavior()
    return limits.copy(
        attributeCountLimit = limits.attributeCountLimit ?: attributeCountLimit,
        attributeValueLengthLimit = limits.attributeValueLengthLimit ?: attributeValueLengthLimit,
    )
}
