package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import net.mamoe.yamlkt.Yaml
import okio.FileSystem
import okio.Path

/**
 * Parses declarative-configuration YAML into the [OpenTelemetryConfiguration] schema model.
 *
 * Unlike [YamlConfigParser], which returns the raw parsed value, this maps the document onto the
 * types generated from the `opentelemetry-configuration` JSON schema.
 */
@ExperimentalApi
class OpenTelemetryConfigurationParser {

    /**
     * Parses a single YAML document from [yaml] into an [OpenTelemetryConfiguration].
     */
    fun parse(yaml: String): OpenTelemetryConfiguration =
        Yaml.decodeFromString(OpenTelemetryConfiguration.serializer(), yaml)

    /**
     * Reads the file at [path] from [fileSystem] and parses it as a single YAML document.
     */
    fun parse(fileSystem: FileSystem, path: Path): OpenTelemetryConfiguration =
        parse(fileSystem.read(path) { readUtf8() })
}
