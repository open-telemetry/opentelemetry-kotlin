package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.BufferedSource

/**
 * Abstracts away file system operations that are used to persist telemetry.
 */
@ExperimentalApi
internal interface TelemetryFileSystem {

    /**
     * Writes a [BufferedSource] to a file with the given filename.
     * @return true if the write succeeded
     */
    fun write(filename: String, source: BufferedSource): Boolean

    /**
     * Lists all filenames in the directory.
     */
    fun list(): List<String>

    /**
     * Deletes the file matching the given filename.
     */
    fun delete(filename: String)

    /**
     * Reads a [BufferedSource] from a file with the given filename, or returns null if the
     * file can't be read.
     *
     * The [BufferedSource] must be closed after it is read. Consider [use] to read
     * this safely.
     */
    fun read(filename: String): BufferedSource?
}
