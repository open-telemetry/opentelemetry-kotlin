package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextStorage

/**
 * Stores the current context in opentelemetry-java's `ContextStorage`, so that Kotlin and Java
 * code can share a single current context. This is only relevant when using [toOtelJavaApi].
 */
@ExperimentalApi
internal class OtelJavaCurrentContextStorage(
    rootSupplier: () -> Context,
) : ScopedCurrentContextStorage {

    private val root by lazy { rootSupplier() }

    override fun attach(context: Context): Scope {
        val scope = OtelJavaContextStorage.get().attach(context.toOtelJavaContext())
        return ScopeAdapter(scope)
    }

    override fun setCurrent(context: Context) {
        OtelJavaContextStorage.get().attach(context.toOtelJavaContext())
    }

    override fun current(): Context {
        return when (val current = OtelJavaContext.current()) {
            OtelJavaContext.root() -> root
            else -> current.toOtelKotlinContext()
        }
    }
}
