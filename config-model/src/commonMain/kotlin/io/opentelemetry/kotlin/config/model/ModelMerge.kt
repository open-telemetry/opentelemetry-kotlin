package io.opentelemetry.kotlin.config.model

/**
 * Merges two optional nodes, where [higher] comes from the higher-precedence layer.
 *
 * A `null` node means the layer said nothing about it, so the other layer's node is used as-is.
 */
internal fun <T : ConfigModel<T>> mergeNode(lower: T?, higher: T?): T? = when {
    lower == null -> higher
    higher == null -> lower
    else -> lower.mergeWith(higher)
}
