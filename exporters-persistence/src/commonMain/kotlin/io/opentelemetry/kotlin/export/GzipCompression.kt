package io.opentelemetry.kotlin.export

import okio.BufferedSource

/**
 * Compresses the source using gzip if supported on the current platform.
 * Returns the original source unchanged on platforms without gzip support.
 */
internal expect fun gzipCompress(source: BufferedSource): BufferedSource

/**
 * Decompresses a gzip-compressed source if supported on the current platform.
 * Returns the original source unchanged on platforms without gzip support.
 */
internal expect fun gzipDecompress(source: BufferedSource): BufferedSource
