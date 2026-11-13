package io.opentelemetry.kotlin.factory

class FakeIdGenerator : IdGenerator {
    override fun generateSpanIdBytes(): ByteArray = "1234561234561234".hexToByteArray()
    override fun generateTraceIdBytes(): ByteArray = "12345612345612341234561234561234".hexToByteArray()
}
