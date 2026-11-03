package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.export.OperationResultCode
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Shuts down each distinct sampler once, returning [OperationResultCode.Success] only if every
 * sampler succeeds. Samplers that appear more than once (e.g. the shared defaults in
 * [ParentBasedSampler]) are only shut down once.
 *
 * Delegates may be user-supplied, so one that throws does not stop the others from being shut
 * down. The first failure is rethrown once every delegate has been called, so the caller's guard
 * can report it.
 */
internal suspend fun shutdownSamplers(samplers: Collection<Sampler>): OperationResultCode {
    var success = true
    var failure: Throwable? = null
    samplers.distinct().forEach {
        try {
            success = it.shutdown() == OperationResultCode.Success && success
        } catch (exc: Throwable) {
            currentCoroutineContext().ensureActive()
            success = false
            if (failure == null) {
                failure = exc
            }
        }
    }
    failure?.let { throw it }
    return when {
        success -> OperationResultCode.Success
        else -> OperationResultCode.Failure
    }
}
