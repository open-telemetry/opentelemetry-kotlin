package io.opentelemetry.kotlin.context

internal actual fun threadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
): ImplicitContextStorage = JvmThreadLocalImplicitContextStorage(rootSupplier)

private class JvmThreadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
) : ImplicitContextStorage {

    private val root by lazy { rootSupplier() }
    private val current = ThreadLocal<Context>()

    override fun setImplicitContext(context: Context) {
        current.set(context)
    }

    override fun implicitContext(): Context = current.get() ?: root
}
