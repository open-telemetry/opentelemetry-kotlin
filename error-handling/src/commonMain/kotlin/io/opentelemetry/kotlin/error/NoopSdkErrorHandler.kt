package io.opentelemetry.kotlin.error

/**
 * An [SdkErrorHandler] that silently discards everything reported to it.
 */
public object NoopSdkErrorHandler : SdkErrorHandler {

    override fun onError(error: SdkError) {
    }
}
