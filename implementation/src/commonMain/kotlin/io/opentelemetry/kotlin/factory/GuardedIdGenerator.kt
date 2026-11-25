package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ReportOnce
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.error.reportUserCodeError
import io.opentelemetry.kotlin.propagation.utils.SPAN_ID_BYTES
import io.opentelemetry.kotlin.propagation.utils.TRACE_ID_BYTES
import io.opentelemetry.kotlin.propagation.utils.isAllZeroBytes

internal class GuardedIdGenerator(
    private val delegate: IdGenerator,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: IdGenerator = IdGeneratorImpl(),
) : IdGenerator {

    private val invalidSpanId = ReportOnce()
    private val invalidTraceId = ReportOnce()
    private val spanIdFailure = ReportOnce()
    private val traceIdFailure = ReportOnce()
    private val randomTraceIdsFailure = ReportOnce()

    override fun generateSpanIdBytes(): ByteArray =
        guardId(
            member = "generateSpanIdBytes",
            expectedSize = SPAN_ID_BYTES,
            invalid = invalidSpanId,
            failure = spanIdFailure,
            fallback = fallback::generateSpanIdBytes,
            action = delegate::generateSpanIdBytes,
        )

    override fun generateTraceIdBytes(): ByteArray =
        guardId(
            member = "generateTraceIdBytes",
            expectedSize = TRACE_ID_BYTES,
            invalid = invalidTraceId,
            failure = traceIdFailure,
            fallback = fallback::generateTraceIdBytes,
            action = delegate::generateTraceIdBytes,
        )

    override val generatesRandomTraceIds: Boolean
        get() = try {
            delegate.generatesRandomTraceIds
        } catch (exc: Throwable) {
            reportFailure("generatesRandomTraceIds", randomTraceIdsFailure, exc)
            false
        }

    private fun reportFailure(member: String, failure: ReportOnce, exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(
            exc,
            "IdGenerator.$member failed, using fallback. Later failures are not reported.",
            failure,
        )
    }

    /**
     * Runs [action], which may be user-supplied code, then checks the returned ID. An ID that is
     * not [expectedSize] bytes or is all zero falls back to the built-in generator, which produces
     * valid ones. Each generator member reports its invalid IDs and its failures once each, on the
     * first occurrence.
     */
    private inline fun guardId(
        member: String,
        expectedSize: Int,
        invalid: ReportOnce,
        failure: ReportOnce,
        fallback: () -> ByteArray,
        action: () -> ByteArray,
    ): ByteArray = try {
        val id = action()
        if (id.size == expectedSize && !id.isAllZeroBytes()) {
            id
        } else {
            if (invalid.tryAcquire()) {
                reportInvalidId(member, expectedSize, id.size)
            }
            fallback()
        }
    } catch (exc: Throwable) {
        reportFailure(member, failure, exc)
        fallback()
    }

    private fun reportInvalidId(member: String, expectedSize: Int, actualSize: Int) {
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "IdGenerator",
                message = "IdGenerator.$member returned an invalid ID (expected $expectedSize " +
                    "non-zero bytes, got $actualSize), using fallback. " +
                    "Later invalid IDs from this member are not reported.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
}

internal fun IdGenerator.guarded(sdkErrorHandler: SdkErrorHandler): IdGenerator = when (this) {
    is IdGeneratorImpl, is GuardedIdGenerator -> this
    else -> GuardedIdGenerator(this, sdkErrorHandler)
}
