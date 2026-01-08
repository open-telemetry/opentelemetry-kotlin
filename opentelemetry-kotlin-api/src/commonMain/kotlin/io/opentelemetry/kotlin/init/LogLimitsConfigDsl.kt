package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines limits on how much data should be captured in spans.
 */
@ExperimentalApi
@ConfigDsl
public interface LogLimitsConfigDsl {

    /**
     * The maximum number of attributes
     */
    public var attributeCountLimit: Int

    /**
     * The maximum length of an attribute value
     */
    public var attributeValueLengthLimit: Int
}
