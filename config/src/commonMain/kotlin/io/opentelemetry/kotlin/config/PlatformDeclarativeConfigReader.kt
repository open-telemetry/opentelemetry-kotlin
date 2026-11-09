package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The reader for declarative config files on the platform the SDK is running on, or `null` where
 * the SDK does not read them. Currently declarative config files are only read on the JVM (not Android)
 */
@ExperimentalApi
internal expect fun platformDeclarativeConfigReader(): DeclarativeConfigReader?
