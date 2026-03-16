package io.opentelemetry.kotlin.context
class FakeScope(private val onDetach: () -> Boolean = { true }) : Scope {
    override fun detach(): Boolean = onDetach()
}
