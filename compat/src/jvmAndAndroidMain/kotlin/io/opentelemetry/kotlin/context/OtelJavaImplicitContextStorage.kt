package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextStorage

/**
 * An [ImplicitContextStorage] that delegates to whichever [OtelJavaContextStorage] is configured on
 * opentelemetry-java. This ensures the Kotlin and Java APIs share the same implicit context.
 */
internal object OtelJavaImplicitContextStorage : ImplicitContextStorage {

    override fun setImplicitContext(context: Context) {
        OtelJavaContextStorage.get().attach(context.toOtelJavaContext())
    }

    override fun implicitContext(): Context = OtelJavaContext.current().toOtelKotlinContext()
}
