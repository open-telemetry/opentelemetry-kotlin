package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior

/**
 * Hands the resolved log limits to the Java SDK. Only the limits that were configured are set, so
 * anything left unset falls back to the Java SDK's own default.
 */
internal fun AttributeLimitsBehavior.toOtelJavaLogLimits(): OtelJavaLogLimits =
    OtelJavaLogLimits.builder().apply {
        attributeCountLimit?.let(::setMaxNumberOfAttributes)
        attributeValueLengthLimit?.let(::setMaxAttributeValueLength)
    }.build()
