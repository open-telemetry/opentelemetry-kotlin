package io.opentelemetry.kotlin.factory

internal object NoopIdGenerator : IdGenerator {
    override val invalidTraceId: ByteArray = ByteArray(16)
    override val invalidSpanId: ByteArray = ByteArray(8)
    override fun generateSpanIdBytes(): ByteArray = invalidSpanId
    override fun generateTraceIdBytes(): ByteArray = invalidTraceId
}
