package io.opentelemetry.kotlin.export

import android.content.Context
import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Obtains the default directory for writing cache files.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(context: Context): String = "${context.applicationContext.cacheDir}"
