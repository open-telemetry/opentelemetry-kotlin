package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ReportOnce
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError

/**
 * Wraps a user-supplied [IdGenerator] so one that throws cannot destabilize the SDK: the failure
 * is reported to [sdkErrorHandler] and the fallback generator's ID is used instead. The fallback is
 * only consulted after [delegate] throws.
 *
 * It is also the [OtelJavaIdGenerator] handed to the Java SDK. A [delegate] that implements
 * [OtelJavaIdGenerator] itself is read through that interface, exactly as it would be unwrapped;
 * any other is adapted from its byte-array methods.
 */
internal class GuardedCompatIdGenerator(
    private val delegate: IdGenerator,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: IdGenerator = CompatIdGenerator(),
) : IdGenerator, OtelJavaIdGenerator {

    private val javaDelegate: OtelJavaIdGenerator =
        delegate as? OtelJavaIdGenerator ?: OtelJavaIdGeneratorAdapter(delegate)

    private val spanIdBytesFailure = ReportOnce()
    private val traceIdBytesFailure = ReportOnce()
    private val randomTraceIdsPropertyFailure = ReportOnce()
    private val spanIdFailure = ReportOnce()
    private val traceIdFailure = ReportOnce()
    private val randomTraceIdsFailure = ReportOnce()

    override fun generateSpanIdBytes(): ByteArray =
        guard(
            "generateSpanIdBytes",
            spanIdBytesFailure,
            fallback::generateSpanIdBytes,
            delegate::generateSpanIdBytes,
        )

    override fun generateTraceIdBytes(): ByteArray =
        guard(
            "generateTraceIdBytes",
            traceIdBytesFailure,
            fallback::generateTraceIdBytes,
            delegate::generateTraceIdBytes,
        )

    override val generatesRandomTraceIds: Boolean
        get() = guard(
            "generatesRandomTraceIds",
            randomTraceIdsPropertyFailure,
            { false },
            delegate::generatesRandomTraceIds,
        )

    override fun generateSpanId(): String =
        guard(
            "generateSpanId",
            spanIdFailure,
            { fallback.generateSpanIdBytes().toHexString() },
            javaDelegate::generateSpanId,
        )

    override fun generateTraceId(): String =
        guard(
            "generateTraceId",
            traceIdFailure,
            { fallback.generateTraceIdBytes().toHexString() },
            javaDelegate::generateTraceId,
        )

    override fun generatesRandomTraceIds(): Boolean =
        guard("generatesRandomTraceIds", randomTraceIdsFailure, { false }, javaDelegate::generatesRandomTraceIds)

    private inline fun <T> guard(
        member: String,
        failure: ReportOnce,
        fallback: () -> T,
        action: () -> T,
    ): T = try {
        action()
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(
            exc,
            "IdGenerator.$member failed, using fallback. Later failures are not reported.",
            failure,
        )
        fallback()
    }
}

/**
 * Wraps [this] when it is user-supplied, so its failures are reported rather than escaping into
 * the Java SDK. Generators the SDK itself adapted are returned unwrapped.
 */
internal fun IdGenerator.guarded(sdkErrorHandler: SdkErrorHandler): IdGenerator = when (this) {
    is CompatIdGenerator, is GuardedCompatIdGenerator -> this
    else -> GuardedCompatIdGenerator(this, sdkErrorHandler)
}
