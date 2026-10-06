package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaKeyValue
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.aliases.OtelJavaValueType
import io.opentelemetry.kotlin.logging.normalizeLogBody
import java.nio.ByteBuffer

/**
 * Converts a log record body into opentelemetry-java's [OtelJavaValue] so that structured
 * [AnyValue] bodies keep their shape.
 */
internal fun Any.toOtelJavaValue(): OtelJavaValue<*> = when (val body = normalizeLogBody(this)) {
    null -> OtelJavaValue.empty()
    is AnyValue -> body.toOtelJavaValue()
    is String -> OtelJavaValue.of(body)
    is Boolean -> OtelJavaValue.of(body)
    is Long -> OtelJavaValue.of(body)
    is Double -> OtelJavaValue.of(body)
    else -> OtelJavaValue.of(body.toString())
}

internal fun AnyValue.toOtelJavaValue(): OtelJavaValue<*> = when (this) {
    AnyValue.NullValue -> OtelJavaValue.empty()
    is AnyValue.StringValue -> OtelJavaValue.of(value)
    is AnyValue.BoolValue -> OtelJavaValue.of(value)
    is AnyValue.LongValue -> OtelJavaValue.of(value)
    is AnyValue.DoubleValue -> OtelJavaValue.of(value)
    is AnyValue.BytesValue -> OtelJavaValue.of(value)
    is AnyValue.ListValue -> OtelJavaValue.of(values.map { it.toOtelJavaValue() })
    is AnyValue.MapValue -> OtelJavaValue.of(values.mapValues { it.value.toOtelJavaValue() })
}

/**
 * Converts an opentelemetry-java log record body back into its Kotlin representation.
 */
internal fun OtelJavaValue<*>.toOtelKotlinBody(): Any? = when (type) {
    OtelJavaValueType.STRING,
    OtelJavaValueType.BOOLEAN,
    OtelJavaValueType.LONG,
    OtelJavaValueType.DOUBLE -> value
    OtelJavaValueType.EMPTY, null -> null
    else -> toOtelKotlinAnyValue()
}

internal fun OtelJavaValue<*>?.toOtelKotlinAnyValue(): AnyValue {
    val payload = this?.value
    val result = when (this?.type) {
        OtelJavaValueType.STRING -> (payload as? String)?.let(AnyValue::StringValue)
        OtelJavaValueType.BOOLEAN -> (payload as? Boolean)?.let(AnyValue::BoolValue)
        OtelJavaValueType.LONG -> (payload as? Long)?.let(AnyValue::LongValue)
        OtelJavaValueType.DOUBLE -> (payload as? Double)?.let(AnyValue::DoubleValue)
        OtelJavaValueType.BYTES -> (payload as? ByteBuffer)?.let { AnyValue.BytesValue(it.toByteArray()) }
        OtelJavaValueType.ARRAY -> (payload as? List<*>)?.let { list ->
            AnyValue.ListValue(list.map { (it as? OtelJavaValue<*>).toOtelKotlinAnyValue() })
        }
        OtelJavaValueType.KEY_VALUE_LIST -> (payload as? List<*>)?.let { list ->
            AnyValue.MapValue(
                list.filterIsInstance<OtelJavaKeyValue>().associate {
                    it.key to it.value.toOtelKotlinAnyValue()
                }
            )
        }
        OtelJavaValueType.EMPTY, null -> null
    }
    return result ?: AnyValue.NullValue
}

private fun ByteBuffer.toByteArray(): ByteArray {
    val buf = duplicate()
    return ByteArray(buf.remaining()).also { buf.get(it) }
}
