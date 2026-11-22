package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault

/**
 * Wraps a user-supplied [IdGenerator] so one that throws cannot destabilize the SDK: the failure
 * is reported to [sdkErrorHandler] and the fallback generator's ID is used instead.
 */
internal class GuardedCompatIdGenerator(
    private val delegate: IdGenerator,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: IdGenerator = CompatIdGenerator(),
) : IdGenerator {

    override fun generateSpanIdBytes(): ByteArray =
        sdkErrorHandler.guardOrDefault(fallback.generateSpanIdBytes(), "IdGenerator.generateSpanIdBytes failed, using fallback") {
            delegate.generateSpanIdBytes()
        }

    override fun generateTraceIdBytes(): ByteArray =
        sdkErrorHandler.guardOrDefault(fallback.generateTraceIdBytes(), "IdGenerator.generateTraceIdBytes failed, using fallback") {
            delegate.generateTraceIdBytes()
        }

    override val generatesRandomTraceIds: Boolean
        get() = sdkErrorHandler.guardOrDefault(false, "IdGenerator.generatesRandomTraceIds failed, using fallback") {
            delegate.generatesRandomTraceIds
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
