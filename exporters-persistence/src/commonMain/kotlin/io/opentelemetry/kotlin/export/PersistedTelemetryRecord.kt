package io.opentelemetry.kotlin.export

/**
 * Creates a filename that can be used to persist a telemetry record.
 */
internal data class PersistedTelemetryRecord(
    val timestamp: Long,
    val type: PersistedTelemetryType,
    val uid: String,
) {

    /**
     * String filename that encodes information about the telemetry record.
     */
    val filename: String = "${type}_${timestamp}_$uid.gz"

    companion object {

        private const val EXTENSION = ".gz"
        private const val DELIMITER = "_"

        /**
         * Comparator that orders records by timestamp (oldest first), then type, then uid.
         */
        val comparator: Comparator<PersistedTelemetryRecord> = compareBy(
            { it.timestamp },
            { it.type },
            { it.uid }
        )

        /**
         * Decodes a filename to gain information about the telemetry recorded within.
         *
         * Returns null if the filename does not match the expected format.
         */
        fun fromFilename(filename: String): PersistedTelemetryRecord? {
            if (!filename.endsWith(EXTENSION)) {
                return null
            }
            val parts = filename.removeSuffix(EXTENSION).split(DELIMITER, limit = 3)
            if (parts.size != 3) {
                return null
            }
            val type = try {
                PersistedTelemetryType.valueOf(parts[0])
            } catch (e: IllegalArgumentException) {
                return null
            }
            val timestamp = parts[1].toLongOrNull() ?: return null
            return PersistedTelemetryRecord(timestamp, type, parts[2])
        }
    }
}
