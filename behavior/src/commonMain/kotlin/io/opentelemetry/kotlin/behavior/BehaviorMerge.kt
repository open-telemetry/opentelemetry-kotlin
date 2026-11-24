package io.opentelemetry.kotlin.behavior

/**
 * Merges two optional nodes, where [higher] comes from the higher-precedence layer.
 *
 * A `null` node means the layer said nothing about it, so the other layer's node is used as-is.
 */
internal fun <T : Behavior<T>> mergeNode(lower: T?, higher: T?): T? = when {
    lower == null -> higher
    higher == null -> lower
    else -> lower.mergeWith(higher)
}

/**
 * Merges two optional maps, where entries from [higher] win on key collisions.
 */
internal fun <K, V> mergeMap(lower: Map<K, V>?, higher: Map<K, V>?): Map<K, V>? = when {
    lower == null -> higher
    higher == null -> lower
    else -> lower + higher
}

/**
 * Merges two optional lists, where entries from [higher] are appended to [lower].
 *
 * A `null` list means the layer said nothing about it, so the other layer's list is used as-is.
 */
internal fun <T : Behavior<T>> mergeList(lower: List<T>?, higher: List<T>?): List<T>? = when {
    lower == null -> higher
    higher == null -> lower
    else -> lower + higher
}
