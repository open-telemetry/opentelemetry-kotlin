package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.propagation.W3CTraceStateCodec
import io.opentelemetry.kotlin.propagation.W3CTraceStateValidator
import io.opentelemetry.kotlin.resource.MutableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.hex
import io.opentelemetry.proto.common.v1.InstrumentationScope

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

internal fun io.opentelemetry.proto.resource.v1.Resource.toResource(): Resource =
    DeserializedResource(attributes = attributes.toAttributeMap())

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
    override val traceIdBytes: ByteArray,
    override val spanIdBytes: ByteArray,
    flags: Int = 0,
    traceStateString: String = "",
    override val isRemote: Boolean = false,
) : SpanContext {
    override val traceId: String by lazy { traceIdBytes.toHexString() }
    override val spanId: String by lazy { spanIdBytes.toHexString() }
    override val traceFlags: TraceFlags = DeserializedTraceFlags(flags and 0xFF)
    override val isValid: Boolean by lazy { traceId != "0".repeat(32) && spanId != "0".repeat(16) }
    override val traceState: TraceState by lazy {
        DeserializedTraceState(W3CTraceStateCodec.decode(traceStateString))
    }
}

private class DeserializedTraceFlags(private val value: Int) : TraceFlags {
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
