package io.opentelemetry.kotlin.export

import okio.BufferedSource

// Okio doesn't support gzip on JS yet: https://github.com/square/okio/issues/1550

internal actual fun gzipCompress(source: BufferedSource): BufferedSource {
    return source
}

internal actual fun gzipDecompress(source: BufferedSource): BufferedSource {
    return source
}
