package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A simple implementation of [ImplicitContextStorage] that only allows one context at any time,
 * and doesn't use thread locals/coroutine context to distinguish between what is current.
 */
@OptIn(ExperimentalApi::class)
internal class DefaultImplicitContextStorage(
    rootSupplier: () -> Context
) : ImplicitContextStorage {

    private val root by lazy { rootSupplier() }
    private var current: Context? = null

    override fun setImplicitContext(context: Context) {
        current = context
    }

    override fun implicitContext(): Context = current ?: root
}
