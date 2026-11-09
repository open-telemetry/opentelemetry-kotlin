package io.opentelemetry.kotlin

import okio.FileSystem

public actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
