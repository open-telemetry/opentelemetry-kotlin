package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

@ExperimentalApi
internal actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM

/**
 * Obtains the default directory for writing cache files.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(): Path =
    "${System.getProperty("java.io.tmpdir")}".toPath()
