package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextStorage

/**
 * Creates a [CurrentContextStorage] that stores the current context in opentelemetry-java's
 * `ContextStorage`, so that Kotlin and Java code share one current context. Use it if you call
 * [io.opentelemetry.kotlin.toOtelJavaApi], by passing it to `context { storage = … }` when
 * creating the SDK. It respects any `ContextStorage` wrappers that are installed (e.g. by the
 * Java agent).
 *
 * Each call returns a new instance, but every instance reads and writes opentelemetry-java's
 * single process-wide `ContextStorage`, so contexts attached through one are visible through all
 * of them.
 */
@ExperimentalApi
public fun createOtelJavaCurrentContextStorage(): CurrentContextStorage = OtelJavaCurrentContextStorage()

@ExperimentalApi
internal class OtelJavaCurrentContextStorage : CurrentContextStorage {

    private val root = rootContext()

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
