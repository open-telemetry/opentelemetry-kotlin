package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.OpenTelemetrySdk

/**
 * Records every SDK a test creates so that it can be shut down in `@AfterTest`. SDKs that are not
 * shut down leak process-wide state (such as the installed context storage) into later tests.
 *
 * ```
 * private val sdks = SdkTracker()
 *
 * @AfterTest
 * fun tearDown() = sdks.shutdownAll()
 *
 * val otel = createOpenTelemetry { ... }.also(sdks::track)
 * ```
 */
class SdkTracker {
    private val sdks = mutableListOf<OpenTelemetry>()

    fun track(sdk: OpenTelemetry) {
        sdks.add(sdk)
    }

    fun shutdownAll() {
        sdks.forEach { it.shutdownForTest() }
        sdks.clear()
    }
}

/**
 * Shuts down this instance if it is an [OpenTelemetrySdk]. Shutting down an SDK that is already
 * shut down is harmless. Blocks until shutdown completes.
 */
fun OpenTelemetry.shutdownForTest() {
    val sdk = this as? OpenTelemetrySdk ?: return
    runShutdown { sdk.shutdown() }
}

internal expect fun runShutdown(action: suspend () -> Unit)
