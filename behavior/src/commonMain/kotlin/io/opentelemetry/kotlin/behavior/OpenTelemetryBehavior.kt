package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior of the SDK, as supplied by one configuration mechanism.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
data class OpenTelemetryBehavior(
    /**
     * The file format version.
     */
    val fileFormat: String? = null,
    /**
     * Defines if the SDK is disabled or not.
     */
    val disabled: Boolean? = null,
    /**
     * Log level of the internal logger, which in this repo is
     * [io.opentelemetry.kotlin.error.SdkErrorHandler] rather than a logger.
     */
    val logLevel: SeverityLevel? = null,
    /**
     * Distro-specific parameters.
     * Out of scope for this repo, will be parsed and ignored with no failures on it.
     */
    val distribution: Distribution? = null,
    /**
     * Entity information associated with the resource.
     *
     * Note: Only supported by the environment variable spec, has no node in the declarative schema
     */
    val entities: String? = null,
    /**
     * The service producing telemetry.
     */
    val resource: ResourceBehavior? = null,
    /**
     * Global limits on attribute capture.
     */
    val attributeLimits: AttributeLimitsBehavior? = null,
    /**
     * Tracer provider.
     */
    val tracerProvider: TracerProviderBehavior? = null,
    /**
     * Logger provider.
     */
    val loggerProvider: LoggerProviderBehavior? = null,
) : Behavior<OpenTelemetryBehavior> {

    override fun mergeWith(higher: OpenTelemetryBehavior): OpenTelemetryBehavior = copy(
        fileFormat = higher.fileFormat ?: fileFormat,
        disabled = higher.disabled ?: disabled,
        logLevel = higher.logLevel ?: logLevel,
        distribution = mergeMap(distribution, higher.distribution),
        entities = higher.entities ?: entities,
        resource = mergeNode(resource, higher.resource),
        attributeLimits = mergeNode(attributeLimits, higher.attributeLimits),
        tracerProvider = mergeNode(tracerProvider, higher.tracerProvider),
        loggerProvider = mergeNode(loggerProvider, higher.loggerProvider),
    )
}

/**
 * Combines [layers] into a single behavior, where each layer takes precedence over the ones before
 * it.
 */
@ExperimentalApi
fun mergeBehaviors(layers: List<OpenTelemetryBehavior>): OpenTelemetryBehavior =
    layers.fold(OpenTelemetryBehavior()) { merged, layer -> merged.mergeWith(layer) }

typealias Distribution = Map<String, Any?>

enum class SeverityLevel {
    TRACE,
    TRACE2,
    TRACE3,
    TRACE4,
    DEBUG,
    DEBUG2,
    DEBUG3,
    DEBUG4,
    INFO,
    INFO2,
    INFO3,
    INFO4,
    WARN,
    WARN2,
    WARN3,
    WARN4,
    ERROR,
    ERROR2,
    ERROR3,
    ERROR4,
    FATAL,
    FATAL2,
    FATAL3,
    FATAL4,
}
