package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal object NoopScope : Scope {
    override fun detach() {
    }
}
