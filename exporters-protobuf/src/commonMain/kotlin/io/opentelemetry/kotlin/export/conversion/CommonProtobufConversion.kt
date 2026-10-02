package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.propagation.utils.isAllZeroBytes
import io.opentelemetry.kotlin.propagation.utils.isValidSpanIdBytes
import io.opentelemetry.kotlin.propagation.utils.isValidTraceIdBytes
import io.opentelemetry.kotlin.propagation.utils.SPAN_ID_BYTES
import io.opentelemetry.kotlin.propagation.utils.TRACE_ID_BYTES
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateCodec
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateValidator
import io.opentelemetry.kotlin.resource.MutableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.hex
import io.opentelemetry.proto.common.v1.InstrumentationScope
import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * Omit invalid (all-zero) trace/span IDs
 */
internal fun ByteArray.toIdByteString(): ByteString =
    if (isAllZeroBytes()) ByteString.EMPTY else toByteString()

fun InstrumentationScopeInfo.toProtobuf(): InstrumentationScope = InstrumentationScope(
    name = name,
    version = version ?: "",
    attributes = attributes.createKeyValues(),
)

internal fun Resource.toProtobuf() =
    io.opentelemetry.proto.resource.v1.Resource(attributes = attributes.createKeyValues())

internal fun InstrumentationScope.toInstrumentationScopeInfo(
    schemaUrl: String?
): InstrumentationScopeInfo = DeserializedInstrumentationScopeInfo(
    name = name,
    version = version.ifEmpty { null },
    schemaUrl = schemaUrl?.ifEmpty { null },
    attributes = attributes.toAttributeMap()
)

internal fun io.opentelemetry.proto.resource.v1.Resource.toResource(
    schemaUrl: String? = null,
): Resource =
    DeserializedResource(
        attributes = attributes.toAttributeMap(),
        schemaUrl = schemaUrl?.ifEmpty { null },
    )

internal fun TraceFlags.toFlagsInt(): Int = hex.toInt(16)

internal fun TraceState.toW3CString(): String = W3CTraceStateCodec.encode(asMap())

private class DeserializedInstrumentationScopeInfo(
    override val name: String,
    override val version: String?,
    override val schemaUrl: String?,
    override val attributes: Map<String, Any>,
) : InstrumentationScopeInfo

private class DeserializedResource(
    override val attributes: Map<String, Any>,
    override val schemaUrl: String? = null
) : Resource {
    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val mutable = DeserializedMutableResource(attributes.toMutableMap(), schemaUrl)
        mutable.apply(action)
        return DeserializedResource(
            attributes = mutable.attributes.toMap(),
            schemaUrl = mutable.schemaUrl,
        )
    }

    override fun merge(other: Resource): Resource = DeserializedResource(
        attributes = attributes + other.attributes,
        schemaUrl = other.schemaUrl ?: schemaUrl,
    )
}

private class DeserializedMutableResource(
    override val attributes: MutableMap<String, Any>,
    override var schemaUrl: String?,
) : MutableResource

internal class DeserializedSpanContext(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    flags: Int = 0,
    traceStateString: String = "",
    override val isRemote: Boolean = false,
) : SpanContext {
    override val traceIdBytes: ByteArray = if (traceIdBytes.isEmpty()) ByteArray(TRACE_ID_BYTES) else traceIdBytes
    override val spanIdBytes: ByteArray = if (spanIdBytes.isEmpty()) ByteArray(SPAN_ID_BYTES) else spanIdBytes
    override val traceId: String by lazy { this.traceIdBytes.toHexString() }
    override val spanId: String by lazy { this.spanIdBytes.toHexString() }
    override val traceFlags: TraceFlags = DeserializedTraceFlags(flags and 0xFF)
    override val isValid: Boolean by lazy {
        this.traceIdBytes.isValidTraceIdBytes() && this.spanIdBytes.isValidSpanIdBytes()
    }
    override val traceState: TraceState by lazy {
        DeserializedTraceState(W3CTraceStateCodec.decode(traceStateString))
    }
}

private class DeserializedTraceFlags(value: Int) : TraceFlags {
    override val isSampled: Boolean = (value and 0x01) != 0
    override val isRandom: Boolean = (value and 0x02) != 0
}

private class DeserializedTraceState(private val entries: Map<String, String>) : TraceState {
    override fun get(key: String): String? = entries[key]
    override fun asMap(): Map<String, String> = entries
    override fun put(key: String, value: String): TraceState = when {
        W3CTraceStateValidator.canPut(entries, key, value) ->
            DeserializedTraceState(entries + (key to value))
        else -> this
    }

    override fun remove(key: String): TraceState = when {
        entries.containsKey(key) -> DeserializedTraceState(entries - key)
        else -> this
    }
}
