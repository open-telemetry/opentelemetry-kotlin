package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator

internal class CompatIdGenerator(
    private val generator: OtelJavaIdGenerator = OtelJavaIdGenerator.random()
) : IdGenerator {
    override fun generateSpanIdBytes(): ByteArray = generator.generateSpanId().hexToByteArray()
    override fun generateTraceIdBytes(): ByteArray = generator.generateTraceId().hexToByteArray()
    override val generatesRandomTraceIds: Boolean = generator.generatesRandomTraceIds()
}
