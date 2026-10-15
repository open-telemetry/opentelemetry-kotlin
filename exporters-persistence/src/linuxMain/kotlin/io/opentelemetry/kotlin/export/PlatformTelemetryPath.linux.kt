package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.getEnvVarValue
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Obtains the default directory for writing cache files, following the XDG Base Directory specification.
 * Falls back to the system temporary directory if neither XDG_CACHE_HOME nor HOME is an absolute path.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(): Path =
    linuxCacheDirectory(getEnvVarValue("XDG_CACHE_HOME"), getEnvVarValue("HOME"))

internal fun linuxCacheDirectory(cacheHome: String?, home: String?): Path = when {
    cacheHome?.startsWith("/") == true -> cacheHome.toPath()
    home?.startsWith("/") == true -> home.toPath() / ".cache"
    else -> FileSystem.SYSTEM_TEMPORARY_DIRECTORY
}
