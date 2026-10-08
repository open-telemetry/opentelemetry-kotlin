package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.SPAN_ID_BYTES
import io.opentelemetry.kotlin.propagation.utils.TRACE_ID_BYTES
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateValidator
import io.opentelemetry.kotlin.propagation.utils.decodeHexOrEmpty
import io.opentelemetry.kotlin.propagation.utils.isValidSpanIdBytes
import io.opentelemetry.kotlin.propagation.utils.isValidTraceIdBytes

private const val SAMPLED_BIT = 0b01
private const val RANDOM_BIT = 0b10

private val INVALID_TRACE_ID_BYTES = ByteArray(TRACE_ID_BYTES)
private val INVALID_SPAN_ID_BYTES = ByteArray(SPAN_ID_BYTES)

// index by the sampled & random bits, as there can only ever be 4 permutations of this immutable object
@OptIn(ExperimentalApi::class)
private val TRACE_FLAGS: Array<TraceFlags> = Array(4) {
    TraceFlagsImpl(isSampled = (it and SAMPLED_BIT) != 0, isRandom = (it and RANDOM_BIT) != 0)
}

@OptIn(ExperimentalApi::class)
private val INVALID_SPAN_CONTEXT: SpanContext = SpanContextImpl(
    traceIdBytes = INVALID_TRACE_ID_BYTES,
    spanIdBytes = INVALID_SPAN_ID_BYTES,
    traceFlags = TRACE_FLAGS[0],
    isRemote = false,
    traceState = TraceStateImpl.EMPTY,
)

/**
 * Returns the invalid [SpanContext], which has all-zero IDs.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@ExperimentalApi
public fun createInvalidSpanContext(): SpanContext = INVALID_SPAN_CONTEXT

/**
 * Creates a [SpanContext] from hex-encoded IDs. Optional properties are configured inside the
 * [action] DSL block.
 *
 * An ID that is not valid hex of the correct length, or that is all zeros, is replaced with
 * all zeros, which makes the [SpanContext] invalid. If [action] throws, the invalid [SpanContext]
 * is returned.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@ExperimentalApi
public fun createSpanContext(
    traceId: String,
    spanId: String,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = createSpanContext(traceId.decodeHexOrEmpty(), spanId.decodeHexOrEmpty(), action)

/**
 * Creates a [SpanContext] from ID bytes. Optional properties are configured inside the
 * [action] DSL block.
 *
 * An ID that is not the correct length, or that is all zeros, is replaced with all zeros, which
 * makes the [SpanContext] invalid. If [action] throws, the invalid [SpanContext] is returned.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@ExperimentalApi
public fun createSpanContext(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext {
    val builder = SpanContextCreationActionImpl()
    try {
        builder.action()
    } catch (ignored: Throwable) {
        return INVALID_SPAN_CONTEXT
    }
    return SpanContextImpl(
        traceIdBytes = traceIdBytes.takeIf { it.isValidTraceIdBytes() } ?: INVALID_TRACE_ID_BYTES,
        spanIdBytes = spanIdBytes.takeIf { it.isValidSpanIdBytes() } ?: INVALID_SPAN_ID_BYTES,
        traceFlags = builder.traceFlags(),
        isRemote = builder.isRemote,
        traceState = builder.traceState,
    )
}

@OptIn(ExperimentalApi::class)
private class SpanContextCreationActionImpl : SpanContextCreationAction {

    override var isSampled: Boolean = false
    override var isRandom: Boolean = false
    override var isRemote: Boolean = false

    var traceState: TraceState = TraceStateImpl.EMPTY

    fun traceFlags(): TraceFlags {
        var index = 0
        if (isSampled) {
            index = index or SAMPLED_BIT
        }
        if (isRandom) {
            index = index or RANDOM_BIT
        }
        return TRACE_FLAGS[index]
    }

    override fun traceState(action: TraceStateCreationAction.() -> Unit) {
        traceState = TraceStateCreationActionImpl().apply(action).build()
    }
}

@OptIn(ExperimentalApi::class)
private class TraceStateCreationActionImpl : TraceStateCreationAction {

    private val entries = linkedMapOf<String, String>()

    override fun put(key: String, value: String) {
        if (W3CTraceStateValidator.canPut(entries, key, value)) {
            entries[key] = value
        }
    }

    fun build(): TraceState = if (entries.isEmpty()) {
        TraceStateImpl.EMPTY
    } else {
        TraceStateImpl(entries)
    }
}
