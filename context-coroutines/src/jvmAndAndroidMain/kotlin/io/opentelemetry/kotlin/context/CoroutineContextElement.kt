package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Returns a [CoroutineContext.Element] that installs this [Context] as the implicit context whenever
 * a coroutine using it is resumed on a thread. When the coroutine suspends the previous context will
 * be restored. This makes the implicit context follow the coroutine across suspension, thread hops,
 * and into child coroutines.
 *
 * The SDK must be configured with thread-local implicit-context storage (the `THREAD_LOCAL` storage
 * mode) for this to behave correctly. You should also avoid setting the implicit context in
 * any other way if you use this method.
 *
 * This API is only available on JVM/Android as it's backed by
 * `kotlinx.coroutines.ThreadContextElement`, which itself is JVM-only.
 *
 * Example usage:
 *
 * ```
 * withContext(myOtelContext.asCoroutineContextElement()) {
 *     // perform operation
 * }
 * ```
 */
@ExperimentalApi
public fun Context.asCoroutineContextElement(): CoroutineContext.Element = OtelContextElement(this)

/**
 * Bridges a [Context] into the coroutine machinery by attaching it (and restoring the previous
 * context) at each resume/suspend boundary.
 */
internal class OtelContextElement(
    private val context: Context,
) : ThreadContextElement<Scope>, AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<OtelContextElement>

    override fun updateThreadContext(context: CoroutineContext): Scope = this.context.attach()

    override fun restoreThreadContext(context: CoroutineContext, oldState: Scope) {
        oldState.detach()
    }
}
