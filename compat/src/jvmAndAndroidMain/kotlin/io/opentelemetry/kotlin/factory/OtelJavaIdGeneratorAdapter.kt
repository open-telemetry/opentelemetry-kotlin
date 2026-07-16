package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator

/**
 * Wraps a Kotlin [IdGenerator] so it can be supplied to the underlying opentelemetry-java
 * [OtelJavaIdGenerator].
 */
internal class OtelJavaIdGeneratorAdapter(
    private val impl: IdGenerator,
) : OtelJavaIdGenerator {
    override fun generateSpanId(): String = impl.generateSpanIdBytes().toHexString()
    override fun generateTraceId(): String = impl.generateTraceIdBytes().toHexString()
}
