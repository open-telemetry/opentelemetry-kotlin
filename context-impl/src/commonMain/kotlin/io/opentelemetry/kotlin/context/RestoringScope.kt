package io.opentelemetry.kotlin.context

/**
 * A [Scope] that runs [restore] when detached. Misuse checks (double or out-of-order detach) are
 * the caller's responsibility.
 */
internal class RestoringScope(private val restore: () -> Unit) : Scope {
    override fun detach(): Boolean {
        restore()
        return true
    }
}
