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
     * This file is only read on the JVM (not Android). Every other target ignores this path.
     *
     * SDK initialization will error if a YAML file is supplied that cannot be read or does
     * not contain valid configuration.
     */
    public fun configFile(path: String)
}
