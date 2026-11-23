package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.AtomicBoolean
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

    private val invalidSpanIdReported = AtomicBoolean(false)
    private val invalidTraceIdReported = AtomicBoolean(false)

    override fun generateSpanIdBytes(): ByteArray =
        guardId(
            member = "generateSpanIdBytes",
            expectedSize = SPAN_ID_BYTES,
            reported = invalidSpanIdReported,
            fallback = fallback::generateSpanIdBytes,
            action = delegate::generateSpanIdBytes,
        )

    override fun generateTraceIdBytes(): ByteArray =
        guardId(
            member = "generateTraceIdBytes",
            expectedSize = TRACE_ID_BYTES,
            reported = invalidTraceIdReported,
            fallback = fallback::generateTraceIdBytes,
            action = delegate::generateTraceIdBytes,
        )

    override val generatesRandomTraceIds: Boolean
        get() = guard("generatesRandomTraceIds", { false }, delegate::generatesRandomTraceIds)

    private inline fun <T> guard(member: String, fallback: () -> T, action: () -> T): T = try {
        action()
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(exc, "IdGenerator.$member failed, using fallback")
        fallback()
    }

    /**
     * Runs [action], which may be user-supplied code, then checks the returned ID. An ID that is
     * not [expectedSize] bytes or is all zero falls back to the built-in generator, which produces
     * valid ones. Each generator member reports its invalid IDs once, on the first occurrence.
     */
    private inline fun guardId(
        member: String,
        expectedSize: Int,
        reported: AtomicBoolean,
        fallback: () -> ByteArray,
        action: () -> ByteArray,
    ): ByteArray = try {
        val id = action()
        if (id.size == expectedSize && !id.isAllZeroBytes()) {
            id
        } else {
            if (reported.compareAndSet(false, true)) {
                reportInvalidId(member, expectedSize, id.size)
            }
            fallback()
        }
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(exc, "IdGenerator.$member failed, using fallback")
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
