package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.resource.MutableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.TraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceState
import io.opentelemetry.kotlin.tracing.model.hex
import io.opentelemetry.proto.common.v1.InstrumentationScope

@OptIn(ExperimentalApi::class)
fun InstrumentationScopeInfo.toProtobuf(): InstrumentationScope = InstrumentationScope(
    name = name,
    version = version ?: "",
    attributes = attributes.createKeyValues(),
)

@OptIn(ExperimentalApi::class)
internal fun Resource.toProtobuf() =
    io.opentelemetry.proto.resource.v1.Resource(attributes = attributes.createKeyValues())

@OptIn(ExperimentalApi::class)
internal fun InstrumentationScope.toInstrumentationScopeInfo(
    schemaUrl: String?
): InstrumentationScopeInfo = DeserializedInstrumentationScopeInfo(
    name = name,
    version = version.ifEmpty { null },
    schemaUrl = schemaUrl?.ifEmpty { null },
    attributes = attributes.toAttributeMap()
)

@OptIn(ExperimentalApi::class)
internal fun io.opentelemetry.proto.resource.v1.Resource.toResource(): Resource =
    DeserializedResource(attributes = attributes.toAttributeMap())

@OptIn(ExperimentalApi::class)
internal fun TraceFlags.toFlagsInt(): Int = hex.toInt(16)

@OptIn(ExperimentalApi::class)
internal fun TraceState.toW3CString(): String =
    asMap().entries.joinToString(",") { "${it.key}=${it.value}" }

@OptIn(ExperimentalApi::class)
private class DeserializedInstrumentationScopeInfo(
    override val name: String,
    override val version: String?,
    override val schemaUrl: String?,
    override val attributes: Map<String, Any>,
) : InstrumentationScopeInfo

@OptIn(ExperimentalApi::class)
private class DeserializedResource(
    override val attributes: Map<String, Any>,
    override val schemaUrl: String? = null
) : Resource {
    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        throw UnsupportedOperationException()
    }
}

@OptIn(ExperimentalApi::class)
internal class DeserializedSpanContext(
    override val traceIdBytes: ByteArray,
    override val spanIdBytes: ByteArray,
    flags: Int = 0,
    traceStateString: String = "",
) : SpanContext {
    override val traceId: String by lazy { traceIdBytes.toHexString() }
    override val spanId: String by lazy { spanIdBytes.toHexString() }
    override val traceFlags: TraceFlags = DeserializedTraceFlags(flags and 0xFF)
    override val isValid: Boolean by lazy { traceId != "0".repeat(32) && spanId != "0".repeat(16) }
    override val isRemote: Boolean = false
    override val traceState: TraceState = parseTraceState(traceStateString)
}

@OptIn(ExperimentalApi::class)
private class DeserializedTraceFlags(private val value: Int) : TraceFlags {
    override val isSampled: Boolean = (value and 0x01) != 0
    override val isRandom: Boolean = (value and 0x02) != 0
}

@OptIn(ExperimentalApi::class)
private fun parseTraceState(traceStateString: String): TraceState {
    if (traceStateString.isEmpty()) return DeserializedTraceState(emptyMap())
    val map = traceStateString.split(",").associate { entry ->
        val parts = entry.split("=", limit = 2)
        parts[0] to parts[1]
    }
    return DeserializedTraceState(map)
}

@OptIn(ExperimentalApi::class)
private class DeserializedTraceState(private val entries: Map<String, String>) : TraceState {
    override fun get(key: String): String? = entries[key]
    override fun asMap(): Map<String, String> = entries
    override fun put(key: String, value: String): TraceState = throw UnsupportedOperationException()
    override fun remove(key: String): TraceState = throw UnsupportedOperationException()
}
