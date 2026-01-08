package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeScope : Scope {
    override fun detach() {
    }
}
