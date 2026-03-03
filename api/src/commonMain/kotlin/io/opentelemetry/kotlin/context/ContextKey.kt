package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * A key that identifies a value in a [Context].
 */
@ExperimentalApi
@ThreadSafe
public interface ContextKey<T>
