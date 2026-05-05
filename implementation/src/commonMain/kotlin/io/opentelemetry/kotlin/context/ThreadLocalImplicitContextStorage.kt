package io.opentelemetry.kotlin.context

internal expect fun threadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
): ImplicitContextStorage
