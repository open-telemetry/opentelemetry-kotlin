package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Provides access to declarative configuration consumed at runtime by instrumentation libraries.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/#declarative-configuration
 */
@ExperimentalApi
@ThreadSafe
public interface ConfigProvider {

    /**
     * Returns the `instrumentation` section of the declarative configuration, or an empty
     * [ConfigProperties] if none is set.
     */
    public val instrumentationConfig: ConfigProperties
}
