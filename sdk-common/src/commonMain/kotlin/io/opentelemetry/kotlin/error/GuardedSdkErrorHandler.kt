package io.opentelemetry.kotlin.error

/**
 * Decorates an [SdkErrorHandler] so that a throwing implementation cannot destabilize the SDK.
 */
public class GuardedSdkErrorHandler(
    private val delegate: SdkErrorHandler,
) : SdkErrorHandler {

    override fun onError(error: SdkError) {
        try {
            delegate.onError(error)
        } catch (ignored: Throwable) {
            // swallow
        }
    }
}
