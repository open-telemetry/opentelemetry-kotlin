package io.opentelemetry.kotlin.framework

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
internal actual fun runShutdown(action: suspend () -> Unit) {
    GlobalScope.launch { action() }
}
