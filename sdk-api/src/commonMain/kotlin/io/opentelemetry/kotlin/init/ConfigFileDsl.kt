package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines configuration that is supplied by a declarative configuration file.
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
@ConfigDsl
public interface ConfigFileDsl {

    /**
     * Enables declarative configuration, reading the file with [reader]. Declaring this more than
     * once retains the last declaration.
     *
     * The file is read from [path] if supplied, otherwise from the `OTEL_CONFIG_FILE` environment
     * variable. Relative or absolute paths are accepted. Without this declaration no file is
     * read, and a path set in `OTEL_CONFIG_FILE` is reported as an error and ignored.
     *
     * Declarative config support is a separate artifact, so the SDK doesn't pull in a YAML parser
     * unless you ask for it. Add `config-declarative` and pass `yamlConfigReader()`.
     *
     * SDK initialization will error if a file is supplied that cannot be read or does not contain
     * valid configuration.
     */
    public fun configFile(reader: DeclarativeConfigReader, path: String? = null)
}
