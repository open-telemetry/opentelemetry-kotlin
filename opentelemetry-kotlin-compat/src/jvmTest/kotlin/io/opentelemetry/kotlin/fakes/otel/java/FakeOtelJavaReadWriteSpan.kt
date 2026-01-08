@file:Suppress("DEPRECATION")

package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import java.util.concurrent.TimeUnit

/**
 * Only methods on the [readableSpan] interface are valid to call on this fake
 */
internal class FakeOtelJavaReadWriteSpan(
    val readableSpan: OtelJavaReadableSpan = FakeOtelJavaReadableSpan()
) : OtelJavaReadWriteSpan, OtelJavaReadableSpan by readableSpan {

    override fun getInstrumentationScopeInfo(): OtelJavaInstrumentationScopeInfo {
        return readableSpan.instrumentationScopeInfo
    }

    override fun getAttributes(): OtelJavaAttributes {
        return readableSpan.attributes
    }

    override fun <T : Any?> setAttribute(
        key: OtelJavaAttributeKey<T?>,
        value: T?
    ): OtelJavaSpan {
        TODO("Not yet implemented")
    }

    override fun addEvent(name: String, attributes: OtelJavaAttributes): OtelJavaSpan {
        TODO("Not yet implemented")
    }

    override fun addEvent(
        name: String,
        attributes: OtelJavaAttributes,
        timestamp: Long,
        unit: TimeUnit
    ): OtelJavaSpan? {
        TODO("Not yet implemented")
    }

    override fun setStatus(
        statusCode: OtelJavaStatusCode,
        description: String
    ): OtelJavaSpan {
        TODO("Not yet implemented")
    }

    override fun recordException(
        exception: Throwable,
        additionalAttributes: OtelJavaAttributes
    ): OtelJavaSpan {
        TODO("Not yet implemented")
    }

    override fun updateName(name: String): OtelJavaSpan {
        TODO("Not yet implemented")
    }

    override fun end() {
        TODO("Not yet implemented")
    }

    override fun end(timestamp: Long, unit: TimeUnit) {
        TODO("Not yet implemented")
    }

    override fun isRecording(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLatencyNanos(): Long {
        TODO("Not yet implemented")
    }
}
