package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextStorage

/**
 * Stores the current context in opentelemetry-java's `ContextStorage`, so that Kotlin and Java
 * code can share a single current context.
 */
@ExperimentalApi
internal class OtelJavaCurrentContextStorage(
    rootSupplier: () -> Context,
) : CurrentContextStorage {

    private val root by lazy { rootSupplier() }

    override fun attach(context: Context): Scope {
        val java = when {
            context === root -> OtelJavaContext.root()
            else -> OtelJavaContextBridge.toJava(context, layerOverRoot = true)
        }
        return ScopeAdapter(OtelJavaContextStorage.get().attach(java))
    }

    override fun current(): Context {
        return when (val current = OtelJavaContext.current()) {
            OtelJavaContext.root() -> root
            else -> OtelJavaContextBridge.toKotlin(current)
        }
    }
}
