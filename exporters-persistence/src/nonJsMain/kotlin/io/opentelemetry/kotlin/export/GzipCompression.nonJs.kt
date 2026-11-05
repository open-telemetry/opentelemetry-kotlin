package io.opentelemetry.kotlin.export

import okio.Buffer
import okio.BufferedSource
import okio.GzipSink
import okio.GzipSource
import okio.buffer
import okio.use

internal actual fun gzipCompress(source: BufferedSource): BufferedSource {
    val compressed = Buffer()
    GzipSink(compressed).buffer().use { gzipSink ->
        gzipSink.writeAll(source)
    }
    return compressed
}

internal actual fun gzipDecompress(source: BufferedSource): BufferedSource {
    return GzipSource(source).buffer()
}
