package io.opentelemetry.kotlin

import okio.FileSystem

/**
 * Returns an Okio representation of the platform's file system.
 */
public expect fun getFileSystem(): FileSystem
