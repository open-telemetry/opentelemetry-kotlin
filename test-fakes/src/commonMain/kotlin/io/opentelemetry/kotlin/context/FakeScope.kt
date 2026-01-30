package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeScope(private val onDetach: () -> Unit = {}) : Scope {
    override fun detach() {
        onDetach()
    }
}
