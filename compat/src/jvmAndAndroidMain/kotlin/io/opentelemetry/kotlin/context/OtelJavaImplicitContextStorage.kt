package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextStorage

/**
 * Stores the implicit context in opentelemetry-java's `ContextStorage`, so that Kotlin and Java
 * code can share a single implicit context. This is only relevant when using [toOtelJavaApi].
 */
@ExperimentalApi
internal class OtelJavaImplicitContextStorage(
    rootSupplier: () -> Context,
) : ScopedImplicitContextStorage {

    private val root by lazy { rootSupplier() }

    override fun attach(context: Context): Scope {
        val scope = OtelJavaContextStorage.get().attach(context.toOtelJavaContext())
        return ScopeAdapter(scope)
    }

    override fun setImplicitContext(context: Context) {
        OtelJavaContextStorage.get().attach(context.toOtelJavaContext())
    }

    override fun implicitContext(): Context {
        return when (val current = OtelJavaContext.current()) {
            OtelJavaContext.root() -> root
            else -> current.toOtelKotlinContext()
        }
    }
}
