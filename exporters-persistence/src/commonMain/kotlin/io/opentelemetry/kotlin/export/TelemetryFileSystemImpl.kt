package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.buffer

@ExperimentalApi
internal class TelemetryFileSystemImpl(
    private val fileSystem: FileSystem,
    private val directory: Path,
) : TelemetryFileSystem {

    init {
        fileSystem.createDirectories(directory)
    }

    override fun write(filename: String, source: BufferedSource): Boolean {
        val path = directory / filename
        return try {
            fileSystem.write(path) {
                writeAll(gzipCompress(source))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun list(): List<String> {
        return try {
            fileSystem.list(directory).map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun delete(filename: String) {
        val path = directory / filename
        try {
            fileSystem.delete(path)
        } catch (e: Exception) {
            // assume the file was already deleted
        }
    }

    override fun read(filename: String): BufferedSource? {
        val path = directory / filename
        return try {
            if (fileSystem.exists(path)) {
                val source = fileSystem.source(path).buffer()
                if (isGzipped(source)) {
                    gzipDecompress(source)
                } else {
                    source
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isGzipped(source: BufferedSource): Boolean {
        return source.request(2) &&
            source.buffer[0] == GZIP_MAGIC_1 &&
            source.buffer[1] == GZIP_MAGIC_2
    }

    private companion object {
        const val GZIP_MAGIC_1: Byte = 0x1f
        const val GZIP_MAGIC_2: Byte = 0x8b.toByte()
    }
}
