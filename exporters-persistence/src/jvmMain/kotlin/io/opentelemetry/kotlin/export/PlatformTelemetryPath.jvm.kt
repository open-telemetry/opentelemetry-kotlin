package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.Path
import okio.Path.Companion.toPath

/**
 * Obtains the default directory for writing cache files.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(): Path =
    "${System.getProperty("java.io.tmpdir")}".toPath()
