package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class FakeTracingIdFactory : TracingIdFactory {
    override fun generateSpanIdBytes(): ByteArray = "1234561234561234".hexToByteArray()
    override fun generateTraceIdBytes(): ByteArray = "12345612345612341234561234561234".hexToByteArray()
    override val invalidTraceId: ByteArray = "0000000000000000".hexToByteArray()
    override val invalidSpanId: ByteArray = "00000000000000000000000000000000".hexToByteArray()
}
