package io.opentelemetry.kotlin.export

import android.content.Context
import io.opentelemetry.kotlin.ExperimentalApi
import okio.Path
import okio.Path.Companion.toPath

/**
 * Obtains the default directory for writing cache files.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(context: Context): Path =
    "${context.applicationContext.cacheDir}".toPath()
