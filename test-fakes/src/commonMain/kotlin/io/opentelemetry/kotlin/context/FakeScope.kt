package io.opentelemetry.kotlin.context
class FakeScope(private val onDetach: () -> Unit = {}) : Scope {
    override fun detach() {
        onDetach()
    }
}
