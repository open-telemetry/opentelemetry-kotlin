package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.AtomicLong
import platform.Foundation.NSNumber
import platform.Foundation.NSThread
import platform.Foundation.numberWithLong

private val instanceCounter = AtomicLong()

internal actual fun threadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
): ImplicitContextStorage = AppleThreadLocalImplicitContextStorage(rootSupplier)

private class AppleThreadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
) : ImplicitContextStorage {

    private val root by lazy { rootSupplier() }
    private val key: NSNumber = NSNumber.numberWithLong(instanceCounter.incrementAndGet())

    override fun setImplicitContext(context: Context) {
        NSThread.currentThread.threadDictionary.setObject(context, key)
    }

    override fun implicitContext(): Context =
        NSThread.currentThread.threadDictionary.objectForKey(key) as? Context ?: root
}
