package io.opentelemetry.kotlin

/**
 * Derives a dropped count from the total that opentelemetry-java recorded and the number of items
 * it retained.
 */
internal fun droppedCount(total: Int, retained: Int): Int = (total - retained).coerceAtLeast(0)
