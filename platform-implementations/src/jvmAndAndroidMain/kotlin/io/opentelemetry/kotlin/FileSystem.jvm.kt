package io.opentelemetry.kotlin

import okio.FileSystem

@ExperimentalApi
public actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
