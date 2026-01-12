package io.opentelemetry.kotlin

/**
 * Metadata that uniquely identifies the source of telemetry.
 */
@ExperimentalApi
public interface InstrumentationScopeInfo {

    /**
     * The name of the instrumentation scope.
     */
    public val name: String

    /**
     * The version of the instrumentation scope, if set
     */
    public val version: String?

    /**
     * The URL for the schema of the instrumentation scope, if set
     */
    public val schemaUrl: String?

    /**
     * The attributes of the instrumentation scope, if set
     */
    public val attributes: Map<String, Any>
}
