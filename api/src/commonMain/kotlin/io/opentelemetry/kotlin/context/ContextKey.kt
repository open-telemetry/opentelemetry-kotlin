package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * A key that identifies a value in a [Context].
 *
 * Keys are compared by reference, so
 * [io.opentelemetry.kotlin.factory.ContextFactory.createKey] returns a distinct key on every call
 * even when given the same name. The name is used for debugging only and does not identify the
 * value.
 *
 * In practice, this means you MUST retain the [ContextKey] instance and use it as a shared constant.
 */
@ExperimentalApi
@ThreadSafe
public interface ContextKey<T>
