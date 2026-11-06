package io.opentelemetry.kotlin

import kotlinx.coroutines.CoroutineDispatcher

/**
 * The dispatcher that telemetry export should run I/O on.
 */
public expect val ioDispatcher: CoroutineDispatcher
