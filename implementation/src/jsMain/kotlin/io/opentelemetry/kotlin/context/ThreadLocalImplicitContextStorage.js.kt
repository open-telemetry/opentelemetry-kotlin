package io.opentelemetry.kotlin.context

internal actual fun threadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
): ImplicitContextStorage = DefaultImplicitContextStorage(rootSupplier)
