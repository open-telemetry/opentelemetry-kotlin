package io.opentelemetry.kotlin

/**
 * Creates a thread-safe list - i.e. it's possible to concurrently modify the collection without
 * crashing. There may be platform-specific concerns around consistency of the collection.
 */
public expect fun <T> threadSafeList(): MutableList<T>
