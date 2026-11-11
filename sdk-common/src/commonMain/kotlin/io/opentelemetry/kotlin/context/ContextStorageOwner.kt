package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * An SDK instance that may have installed a process-wide [CurrentContextStorage]. Other SDK modules
 * use it to see what an SDK installed without depending on that SDK's internals.
 */
public interface ContextStorageOwner {

    /**
     * The storage this SDK installed into the process-wide slot, or null if it didn't install one.
     */
    public val installedStorage: CurrentContextStorage?

    /**
     * The handler this SDK reports its own errors to.
     */
    public val sdkErrorHandler: SdkErrorHandler
}
