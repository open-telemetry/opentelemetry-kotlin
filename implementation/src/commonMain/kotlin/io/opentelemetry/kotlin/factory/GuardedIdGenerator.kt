package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError

internal class GuardedIdGenerator(
    private val delegate: IdGenerator,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: IdGenerator = IdGeneratorImpl(),
) : IdGenerator {

    override fun generateSpanIdBytes(): ByteArray =
        guard("generateSpanIdBytes", fallback::generateSpanIdBytes, delegate::generateSpanIdBytes)

    override fun generateTraceIdBytes(): ByteArray =
        guard("generateTraceIdBytes", fallback::generateTraceIdBytes, delegate::generateTraceIdBytes)

    override val invalidTraceId: ByteArray
        get() = guard("invalidTraceId", fallback::invalidTraceId, delegate::invalidTraceId)

    override val invalidSpanId: ByteArray
        get() = guard("invalidSpanId", fallback::invalidSpanId, delegate::invalidSpanId)

    override val generatesRandomTraceIds: Boolean
        get() = guard("generatesRandomTraceIds", { false }, delegate::generatesRandomTraceIds)

    private inline fun <T> guard(member: String, fallback: () -> T, action: () -> T): T = try {
        action()
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(exc, "IdGenerator.$member failed, using fallback")
        fallback()
    }
}

internal fun IdGenerator.guarded(sdkErrorHandler: SdkErrorHandler): IdGenerator = when (this) {
    is IdGeneratorImpl, is GuardedIdGenerator -> this
    else -> GuardedIdGenerator(this, sdkErrorHandler)
}
