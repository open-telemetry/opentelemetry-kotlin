package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.propagation.utils.isAllZeroBytes
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateCodec
import io.opentelemetry.kotlin.resource.MutableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.createSpanContext
import io.opentelemetry.kotlin.tracing.hex
import io.opentelemetry.kotlin.proto.common.v1.InstrumentationScope
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
    io.opentelemetry.kotlin.proto.resource.v1.Resource(attributes = attributes.createKeyValues())

internal fun InstrumentationScope.toInstrumentationScopeInfo(
    schemaUrl: String?
): InstrumentationScopeInfo = DeserializedInstrumentationScopeInfo(
    name = name,
    version = version.ifEmpty { null },
    schemaUrl = schemaUrl?.ifEmpty { null },
    attributes = attributes.toAttributeMap()
)

internal fun io.opentelemetry.kotlin.proto.resource.v1.Resource.toResource(
    schemaUrl: String? = null,
): Resource =
    DeserializedResource(
        attributes = attributes.toAttributeMap(),
        schemaUrl = schemaUrl?.ifEmpty { null },
    )

internal fun TraceFlags.toFlagsInt(): Int = hex.toInt(16)

internal fun TraceState.toW3CString(): String = W3CTraceStateCodec.encode(
    try {
        asMap()
    } catch (exc: Throwable) {
        emptyMap()
    }
)

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

internal fun deserializedSpanContext(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    flags: Int = 0,
    traceStateString: String = "",
    isRemote: Boolean = false,
): SpanContext = createSpanContext(traceIdBytes, spanIdBytes) {
    isSampled = (flags and SAMPLED_FLAG) != 0
    isRandom = (flags and RANDOM_FLAG) != 0
    this.isRemote = isRemote
    if (traceStateString.isNotEmpty()) {
        val entries = W3CTraceStateCodec.decode(traceStateString)
        traceState {
            entries.forEach { (key, value) -> put(key, value) }
        }
    }
}

private const val SAMPLED_FLAG = 0x01
private const val RANDOM_FLAG = 0x02
