package io.opentelemetry.kotlin.export

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal suspend fun <T : TelemetryCloseable, R> T.withShutdown(
    block: suspend (T) -> R,
): R = try {
    block(this)
} finally {
    withContext(NonCancellable) {
        shutdown()
    }
}
