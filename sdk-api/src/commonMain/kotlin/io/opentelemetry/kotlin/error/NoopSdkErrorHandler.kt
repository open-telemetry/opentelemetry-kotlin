package io.opentelemetry.kotlin.error

/**
 * An [SdkErrorHandler] that silently discards everything reported to it. This is the default
 * behavior when no handler is configured.
 */
public object NoopSdkErrorHandler : SdkErrorHandler {

    override fun onError(error: SdkError) {
    }
}
