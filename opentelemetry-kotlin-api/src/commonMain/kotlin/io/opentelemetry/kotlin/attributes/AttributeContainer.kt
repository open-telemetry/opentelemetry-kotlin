package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Implementations of this interface returns a read-only snapshot of the 'attributes' as described in the OTel specification.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#attribute
 */
@ExperimentalApi
@ThreadSafe
public interface AttributeContainer {

    /**
     * Returns a snapshot of the attributes as a map.
     */
    @ThreadSafe
    public val attributes: Map<String, Any>
}
