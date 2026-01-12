package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe

/**
 * Limits on log data capture.
 */
@ThreadSafe
internal class LogLimitConfig(

    /**
     * Max attribute count.
     */
    val attributeCountLimit: Int,

    /**
     * Max attribute value length.
     */
    val attributeValueLengthLimit: Int,
)
