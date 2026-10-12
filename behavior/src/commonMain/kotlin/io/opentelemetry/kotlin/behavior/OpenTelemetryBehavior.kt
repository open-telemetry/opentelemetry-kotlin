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
     *
     * Represented as a string including the semver major, minor version numbers (and optionally the meta tag).
     * For example, "0.4", "1.0-rc.2", "1.0" (after stable release).
     *
     * See [VERSIONING.md](https://github.com/open-telemetry/opentelemetry-configuration/blob/main/VERSIONING.md) for
     * more details.
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
    init {
        fileFormat?.let {
            require(FILE_FORMAT_REGEX.matches(fileFormat)) {
                "Invalid file format version: '$fileFormat'. Expected '<major>.<minor>' with an optional pre-release tag."
            }
            val defaultVersion = parseVersion(DEFAULT_FILE_FORMAT_VERSION)
            require(isSupportedVersion(fileFormat)) {
                "Unsupported file format version: '$fileFormat'.\n" +
                    "Supported versions are major=${defaultVersion.major}, minor<=${defaultVersion.minor}."
            }
        }
    }

    private fun isSupportedVersion(version: String): Boolean {
        val version = parseVersion(version)
        val expected = parseVersion(DEFAULT_FILE_FORMAT_VERSION)
        return expected.major == version.major && expected.minor >= version.minor
    }

    private data class Version(
        val major: Int,
        val minor: Int,
        val tag: String?,
    )

    private fun parseVersion(version: String): Version {
        val (numbers, tag) = version.split("-", limit = 2).let {
            it[0] to it.getOrNull(1)
        }

        val (major, minor) = numbers.split(".", limit = 2).map(String::toInt)

        return Version(
            major = major,
            minor = minor,
            tag = tag,
        )
    }

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

    companion object {
        internal const val DEFAULT_FILE_FORMAT_VERSION = "1.2"
        val DEFAULT_LOG_LEVEL = SeverityLevel.INFO

        private val FILE_FORMAT_REGEX = Regex(
            """^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?$"""
        )
    }
}

/**
 * Combines [layers] into a single behavior, where each layer takes precedence over the ones before
 * it.
 */
@ExperimentalApi
fun mergeBehaviors(layers: List<OpenTelemetryBehavior>): OpenTelemetryBehavior =
    layers.fold(OpenTelemetryBehavior(fileFormat = OpenTelemetryBehavior.DEFAULT_FILE_FORMAT_VERSION)) {
            merged, layer ->
        merged.mergeWith(layer)
    }

typealias Distribution = Map<String, Any?>
