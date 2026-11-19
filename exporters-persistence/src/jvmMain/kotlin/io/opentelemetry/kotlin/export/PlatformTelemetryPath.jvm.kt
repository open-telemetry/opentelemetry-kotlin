package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Obtains the default directory for writing cache files.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(): String = "${System.getProperty("java.io.tmpdir")}"
