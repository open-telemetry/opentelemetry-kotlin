package io.opentelemetry.kotlin.factory

internal object NoopIdGenerator : IdGenerator {
    private val invalidTraceId: ByteArray = ByteArray(16)
    private val invalidSpanId: ByteArray = ByteArray(8)
    override fun generateSpanIdBytes(): ByteArray = invalidSpanId
    override fun generateTraceIdBytes(): ByteArray = invalidTraceId
}
