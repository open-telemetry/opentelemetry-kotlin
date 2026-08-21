package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Limits on log record data capture.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecord-limits
 */
@ExperimentalApi
data class LogLimitsBehavior(

    /**
     * Maximum number of attributes that may be recorded on a log record.
     */
    val attributeCountLimit: Int? = null,

    /**
     * Maximum length of a recorded attribute value.
     */
    val attributeValueLengthLimit: Int? = null,
) : Behavior<LogLimitsBehavior> {

    override fun mergeWith(higher: LogLimitsBehavior): LogLimitsBehavior = copy(
        attributeCountLimit = higher.attributeCountLimit ?: attributeCountLimit,
        attributeValueLengthLimit = higher.attributeValueLengthLimit ?: attributeValueLengthLimit,
    )
}
