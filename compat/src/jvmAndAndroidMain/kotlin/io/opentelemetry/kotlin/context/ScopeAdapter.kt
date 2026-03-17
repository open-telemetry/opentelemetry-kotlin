package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaScope

internal class ScopeAdapter(
    private val impl: OtelJavaScope
) : Scope {
    override fun detach(): Boolean {
        impl.close()
        return true
    }
}
