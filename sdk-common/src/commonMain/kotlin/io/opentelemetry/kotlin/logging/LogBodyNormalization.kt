package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AnyValue

private const val MAX_BODY_DEPTH = 16

/**
 * Converts a log record body into an immutable, well-typed value so that it can be safely
 * read on another thread at export time.
 *
 * - [String], [Boolean], [Long], and [Double] are returned as-is. Other integral types are widened
 *   to [Long] and [Float] to [Double]. On JS, where number types can't be distinguished at runtime,
 *   numbers other than [Long] are treated as [Double].
 * - [ByteArray], collections, arrays, maps and [AnyValue] are deep-copied into an [AnyValue].
 *   Map keys are converted via toString(). Nesting deeper than [MAX_BODY_DEPTH] or cyclic
 *   references are replaced with [AnyValue.NullValue].
 * - Anything else is converted via toString().
 *
 * This may run user code (e.g. toString()) so callers must guard against exceptions.
 */
public fun normalizeLogBody(body: Any?): Any? = when (body) {
    null -> null
    is String, is Boolean, is Long, is Double -> body
    is Float -> body.toDouble()
    is Int -> body.toLong()
    is Short -> body.toLong()
    is Byte -> body.toLong()
    is ByteArray, is Collection<*>, is Array<*>, is Map<*, *>, is AnyValue -> body.toImmutableAnyValue(emptyList())
    else -> body.toString()
}

private fun Any?.toImmutableAnyValue(ancestors: List<Any>): AnyValue {
    if (this == null || ancestors.size >= MAX_BODY_DEPTH || ancestors.any { it === this }) {
        return AnyValue.NullValue
    }
    val path = ancestors + this
    return when (this) {
        is AnyValue.ListValue -> AnyValue.ListValue(values.map { it.toImmutableAnyValue(path) })
        is AnyValue.MapValue -> AnyValue.MapValue(values.mapValues { it.value.toImmutableAnyValue(path) })
        is AnyValue.BytesValue -> AnyValue.BytesValue(value.copyOf())
        is AnyValue -> this
        is String -> AnyValue.StringValue(this)
        is Boolean -> AnyValue.BoolValue(this)
        is Long -> AnyValue.LongValue(this)
        is Double -> AnyValue.DoubleValue(this)
        is Float -> AnyValue.DoubleValue(toDouble())
        is Int -> AnyValue.LongValue(toLong())
        is Short -> AnyValue.LongValue(toLong())
        is Byte -> AnyValue.LongValue(toLong())
        is ByteArray -> AnyValue.BytesValue(copyOf())
        is Collection<*> -> AnyValue.ListValue(map { it.toImmutableAnyValue(path) })
        is Array<*> -> AnyValue.ListValue(map { it.toImmutableAnyValue(path) })
        is Map<*, *> -> AnyValue.MapValue(
            entries.associate { (key, value) -> key.toString() to value.toImmutableAnyValue(path) }
        )
        else -> AnyValue.StringValue(toString())
    }
}
