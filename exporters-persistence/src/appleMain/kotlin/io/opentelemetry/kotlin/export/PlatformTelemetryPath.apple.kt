package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
@ExperimentalApi
internal actual fun getCacheDirectory(): Path {
    val cachesDirectory = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String ?: "/tmp"
    return cachesDirectory.toPath()
}

@ExperimentalApi
internal actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
