package io.opentelemetry.kotlin.export

/**
 * A [TelemetryCloseable] that wraps a delegate with a timeout for [forceFlush] and [shutdown].
 */
internal class TimeoutTelemetryCloseable(
    private val delegate: TelemetryCloseable,
    private val flushTimeoutMs: Long = 2000,
    private val shutdownTimeoutMs: Long = MutableShutdownState.DEFAULT_TIMEOUT_MS,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()

    override suspend fun forceFlush(): OperationResultCode =
        runWithTimeout(flushTimeoutMs, delegate::forceFlush)

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(shutdownTimeoutMs, delegate::shutdown)
}
