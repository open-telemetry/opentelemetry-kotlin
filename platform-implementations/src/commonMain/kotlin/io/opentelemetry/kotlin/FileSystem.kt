package io.opentelemetry.kotlin

import okio.FileSystem

/**
 * Returns an Okio representation of the platform's file system.
 */
@ExperimentalApi
public expect fun getFileSystem(): FileSystem
