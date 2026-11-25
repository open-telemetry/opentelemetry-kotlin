package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.context.Context as JavaContext

// region context-compat-bridge
fun bridgeContexts(kotlinContext: Context) {
    // Hand a Kotlin context to opentelemetry-java instrumentation...
    val javaContext: JavaContext = kotlinContext.toOtelJavaContext()

    // ...and turn a Java context back into a Kotlin one, e.g. to use it as a span parent.
    val backToKotlin: Context = javaContext.toOtelKotlinContext()
    println(backToKotlin.span.spanContext.traceId)
}
// end-region
