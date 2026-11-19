package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Obtains the default directory for writing cache files.
 */
@ExperimentalApi
public fun platformDefaultCacheDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    return paths.first() as String
}
