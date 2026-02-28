package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem
import okio.Path

/**
 * Returns an Okio representation of the file system.
 */
@ExperimentalApi
internal expect fun getFileSystem(): FileSystem

/**
 * Returns the directory used to store telemetry for the given [PersistedTelemetryType].
 */
@ExperimentalApi
internal fun getTelemetryStorageDirectory(
    cacheDirectory: Path,
    type: PersistedTelemetryType
): Path {
    return cacheDirectory / "/opentelemetry-kotlin/persisted-telemetry" / type.directoryName
}
