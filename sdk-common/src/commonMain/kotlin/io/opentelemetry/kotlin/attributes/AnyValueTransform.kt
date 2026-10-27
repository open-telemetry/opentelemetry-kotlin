package io.opentelemetry.kotlin.attributes

/**
 * Returns a deep copy of this [AnyValue] that doesn't share any mutable state with the original.
 *
 * - [AnyValue.ListValue], [AnyValue.MapValue], and [AnyValue.BytesValue] are always copied.
 * - [leaf] is applied to every [AnyValue.StringValue] and [AnyValue.BytesValue], e.g. to truncate them.
 *   If it returns its input unchanged then bytes are copied; otherwise the returned value is used as-is
 *   and must not share mutable state with its input.
 * - Immutable values are returned as-is.
 * - Any value nested [maxDepth] or more levels below this one, or any container that is already on the
 *   current path (compared by reference), is replaced with [AnyValue.NullValue].
 */
public fun AnyValue.transform(
    maxDepth: Int = DEFAULT_MAX_ANY_VALUE_DEPTH,
    leaf: (AnyValue) -> AnyValue = { it },
): AnyValue = transform(maxDepth, leaf, ArrayList())

private const val DEFAULT_MAX_ANY_VALUE_DEPTH = 16

private fun AnyValue.transform(
    maxDepth: Int,
    sanitize: (AnyValue) -> AnyValue,
    path: MutableList<AnyValue>,
): AnyValue {
    if (path.size >= maxDepth || path.any { it === this }) {
        return AnyValue.NullValue
    }
    return when (this) {
        is AnyValue.ListValue -> path.visit(this) {
            AnyValue.ListValue(values.map { it.transform(maxDepth, sanitize, path) })
        }
        is AnyValue.MapValue -> path.visit(this) {
            AnyValue.MapValue(values.mapValues { it.value.transform(maxDepth, sanitize, path) })
        }
        is AnyValue.BytesValue -> sanitize(this).let { result ->
            when {
                result === this -> AnyValue.BytesValue(value.copyOf())
                else -> result
            }
        }
        is AnyValue.StringValue -> sanitize(this)
        else -> this
    }
}

private inline fun MutableList<AnyValue>.visit(container: AnyValue, block: () -> AnyValue): AnyValue {
    add(container)
    val result = block()
    removeAt(lastIndex)
    return result
}
