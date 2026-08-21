package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines configuration that is supplied by a declarative configuration file.
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
public interface ConfigFileDsl {

    /**
     * Declares the path of a YAML configuration file, in the format defined by
     * opentelemetry-configuration. Declaring more than one file retains the last one declared.
     *
     * Relative or absolute paths are accepted.
     *
     * The SDK does not read or act on the file yet, so this API has no effect.
     */
    public fun configFile(path: String)
}
