package io.opentelemetry.kotlin.framework

import kotlinx.coroutines.runBlocking

internal actual fun runShutdown(action: suspend () -> Unit) = runBlocking { action() }
