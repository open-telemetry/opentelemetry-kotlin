package io.opentelemetry.kotlin.error

/**
 * The process-wide default [SdkErrorHandler]. Until a delegate is set via
 * [setProcessSdkErrorHandler], errors are buffered (at most 128, the earliest kept).
 */
internal val DEFAULT_SDK_ERROR_HANDLER = BufferingSdkErrorHandler()

/**
 * Sets [delegate] as the target of the process-wide default [SdkErrorHandler]. Buffered errors
 * are replayed to it in order on the calling thread, followed by one WARNING with the dropped
 * count if the buffer overflowed. Later errors are forwarded to it on the thread that reports them.
 *
 * [delegate] may be called from several threads at once, and errors reported during the replay
 * may reach it before buffered ones, so it must be thread-safe. Errors reported from inside a
 * [delegate] call on the same thread are dropped.
 *
 * This function returns false and changes nothing if a delegate is already set, or if it is
 * called from inside a delegate call. Passing null clears any delegate and discards buffered
 * errors, then returns true. Later errors are buffered again. Clearing during a replay does not
 * stop it: the remaining buffered errors still go to the old delegate.
 *
 * Delivery is best-effort. Failures while buffering, replaying or forwarding are swallowed so they
 * never reach the caller, so errors may be lost, e.g. if the process is under memory pressure.
 */
public fun setProcessSdkErrorHandler(delegate: SdkErrorHandler?): Boolean {
    if (delegate == null) {
        DEFAULT_SDK_ERROR_HANDLER.reset()
        return true
    }
    return DEFAULT_SDK_ERROR_HANDLER.setDelegate(delegate)
}
