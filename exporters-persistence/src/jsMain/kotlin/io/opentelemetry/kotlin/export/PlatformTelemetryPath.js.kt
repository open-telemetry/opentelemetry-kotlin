package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem
import okio.Path

@ExperimentalApi
internal actual fun getCacheDirectory(): Path = throw UnsupportedOperationException()

@ExperimentalApi
internal actual fun getFileSystem(): FileSystem = throw UnsupportedOperationException()
