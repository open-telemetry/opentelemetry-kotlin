package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines limits on the capture of attributes.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#attribute-limits
 */
@ExperimentalApi
@ConfigDsl
public interface AttributeLimitsConfigDsl {

    /**
     * The maximum number of attributes
     */
    public var attributeCountLimit: Int

    /**
     * The maximum length of an attribute value
     */
    public var attributeValueLengthLimit: Int
}
