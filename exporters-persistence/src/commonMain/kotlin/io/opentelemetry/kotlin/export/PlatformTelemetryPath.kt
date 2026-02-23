package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem
import okio.Path

/**
 * Returns the base directory that should be used to store cached data.
 */
@ExperimentalApi
internal expect fun getCacheDirectory(): Path

/**
 * Returns an Okio representation of the file system.
 */
@ExperimentalApi
internal expect fun getFileSystem(): FileSystem

/**
 * Returns the directory used to store telemetry for the given [PersistedTelemetryType].
 */
@ExperimentalApi
internal fun getTelemetryStorageDirectory(type: PersistedTelemetryType): Path {
    return getCacheDirectory() / "/opentelemetry-kotlin/persisted-telemetry" / type.directoryName
}
