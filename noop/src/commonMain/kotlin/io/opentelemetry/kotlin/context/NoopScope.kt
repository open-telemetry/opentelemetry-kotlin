package io.opentelemetry.kotlin.context
internal object NoopScope : Scope {
    override fun detach(): Boolean = true
}
