package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.Buffer
import okio.BufferedSource

/**
 * A fake implementation of [TelemetryFileSystem] for testing.
 *
 * This is used instead of [TelemetryFileSystemImpl] with okio's FakeFileSystem because:
 * 1. TelemetryFileSystemImpl uses gzip compression, adding unnecessary complexity to tests
 * 2. This fake provides explicit controls for simulating failure scenarios (failWrites,
 *    failReads, failDeletes) which are difficult to achieve with the real implementation
 */
@OptIn(ExperimentalApi::class)
internal class FakeTelemetryFileSystem : TelemetryFileSystem {
    private val files = mutableMapOf<String, ByteArray>()
    var failWrites = false
    var failReads = false
    var failDeletes = false

    override fun write(filename: String, source: BufferedSource): Boolean {
        if (failWrites) {
            return false
        } else {
            files[filename] = source.readByteArray()
            return true
        }
    }

    override fun list(): List<String> = files.keys.toList()

    override fun delete(filename: String) {
        if (failDeletes) {
            return
        } else {
            files.remove(filename)
        }
    }

    override fun read(filename: String): BufferedSource? {
        if (failReads) {
            return null
        }
        val bytes = files[filename] ?: return null
        return Buffer().write(bytes)
    }
}
