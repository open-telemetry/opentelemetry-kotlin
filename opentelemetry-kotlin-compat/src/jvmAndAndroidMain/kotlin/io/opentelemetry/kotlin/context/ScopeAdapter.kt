package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaScope

@OptIn(ExperimentalApi::class)
internal class ScopeAdapter(
    private val impl: OtelJavaScope
) : Scope {
    override fun detach() = impl.close()
}
