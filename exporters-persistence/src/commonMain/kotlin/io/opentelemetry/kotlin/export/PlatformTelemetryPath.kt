package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.Path

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
