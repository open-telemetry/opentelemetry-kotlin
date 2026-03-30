
package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.factory.toHexString

class FakeSpanContext(
    override val traceIdBytes: ByteArray = ByteArray(16),
    override val spanIdBytes: ByteArray = ByteArray(8),
    override val traceFlags: TraceFlags = FakeTraceFlags(),
    override val traceState: TraceState = FakeTraceState(),
    override val isRemote: Boolean = false,
) : SpanContext {

    companion object {
        val INVALID = FakeSpanContext()
        val VALID = FakeSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
        )
    }

    override val traceId: String = traceIdBytes.toHexString()
    override val spanId: String = spanIdBytes.toHexString()
    override val isValid: Boolean = traceId != "0".repeat(32) && spanId != "0".repeat(16)
}
