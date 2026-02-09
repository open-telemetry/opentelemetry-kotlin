package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Implementations of this interface handle telemetry. The interface allows callers to request a flush of any
 * buffered telemetry, and to perform any necessary cleanup tasks when the SDK is shutting down.
 */
@ExperimentalApi
public interface TelemetryCloseable {

    /**
     * Requests the implementation to flush any buffered telemetry.
     */
    public suspend fun forceFlush(): OperationResultCode

    /**
     * Shuts down the implementation and completes cleanup tasks necessary.
     */
    public suspend fun shutdown(): OperationResultCode
}
