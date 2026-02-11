package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

@ExperimentalApi
internal actual fun getCacheDirectory(): Path {
    val tmpDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
    return tmpDir.toPath()
}

@ExperimentalApi
internal actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
