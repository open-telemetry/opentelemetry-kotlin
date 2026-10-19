package io.opentelemetry.kotlin.smoketest

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.export.TelemetryCloseable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

// Public SDK factories use non-test dispatchers, so waits and timeouts must use real time too.
// Keep cleanup inside runTest: asynchronous @AfterTest hooks are not awaited on JS.
internal fun runSmokeTest(otel: OpenTelemetry, block: suspend () -> Unit) = runTest {
    withContext(Dispatchers.Default) {
        try {
            block()
        } finally {
            withContext(NonCancellable) {
                (otel as TelemetryCloseable).shutdown()
            }
        }
    }
}
