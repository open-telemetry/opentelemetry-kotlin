package io.opentelemetry.kotlin.export

import android.content.Context
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
public fun platformDefaultCacheDirectory(context: Context): Path =
    "${context.applicationContext.cacheDir}".toPath()
