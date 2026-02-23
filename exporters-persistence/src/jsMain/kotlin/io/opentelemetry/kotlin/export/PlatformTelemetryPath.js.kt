package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.FileSystem

@ExperimentalApi
internal actual fun getFileSystem(): FileSystem = throw UnsupportedOperationException()
