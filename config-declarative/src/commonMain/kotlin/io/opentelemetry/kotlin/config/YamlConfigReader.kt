package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.yaml.ConfigFileReaderImpl
import io.opentelemetry.kotlin.config.yaml.toBehavior
import io.opentelemetry.kotlin.init.DeclarativeConfigReader

/**
 * Returns a reader for YAML declarative configuration files, in the format defined by
 * opentelemetry-configuration. Pass it to `configFile()` in the SDK DSL.
 *
 * Files are read from the platform's file system. On platforms without one (JS), reading a file
 * reports an error through the SDK error handler and the file is ignored.
 */
@ExperimentalApi
public fun yamlConfigReader(): DeclarativeConfigReader {
    val fileReader = ConfigFileReaderImpl()
    return object : DeclarativeBehaviorReader {
        override fun read(path: String): OpenTelemetryBehavior = fileReader.read(path).toBehavior()

        override fun readInstrumentation(path: String): ConfigProperties = fileReader.readInstrumentation(path)
    }
}
