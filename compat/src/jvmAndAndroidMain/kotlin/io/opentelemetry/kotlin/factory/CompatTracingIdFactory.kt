package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaSpanId
import io.opentelemetry.kotlin.aliases.OtelJavaTraceId

internal class CompatTracingIdFactory(
    private val generator: OtelJavaIdGenerator = OtelJavaIdGenerator.random()
) : TracingIdFactory {
    override fun generateSpanIdBytes(): ByteArray = generator.generateSpanId().hexToByteArray()
    override fun generateTraceIdBytes(): ByteArray = generator.generateTraceId().hexToByteArray()
    override val invalidTraceId: ByteArray = OtelJavaTraceId.getInvalid().hexToByteArray()
    override val invalidSpanId: ByteArray = OtelJavaSpanId.getInvalid().hexToByteArray()
}
