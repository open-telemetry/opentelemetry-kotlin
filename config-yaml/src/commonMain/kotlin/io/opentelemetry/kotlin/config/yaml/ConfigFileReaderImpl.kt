package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.ConfigProperties
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import io.opentelemetry.kotlin.getFileSystem
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Reads the declarative config file from the platform's file system.
 */
@OptIn(ExperimentalApi::class)
class ConfigFileReaderImpl(
    private val fileSystem: Lazy<FileSystem> = lazy { getFileSystem() },
    private val parser: OpenTelemetryConfigurationParser = OpenTelemetryConfigurationParser(),
) : ConfigFileReader {

    /**
     * The platform file system is resolved here rather than on construction, so that platforms
     * which cannot read files only fail if a config file was actually named.
     */
    override fun read(path: String): OpenTelemetryConfiguration =
        parser.parse(fileSystem.value, path.toPath())

    /**
     * Reads the `.instrumentation` node of the declarative config file at [path]. An absent node
     * reads as empty.
     *
     * Throws if the file cannot be read, or does not hold valid YAML.
     */
    fun readInstrumentation(path: String): ConfigProperties =
        readInstrumentationNode(fileSystem.value, path.toPath()).toConfigProperties()
}
