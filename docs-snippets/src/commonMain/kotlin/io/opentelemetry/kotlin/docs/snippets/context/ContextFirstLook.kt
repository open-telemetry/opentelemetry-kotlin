package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.rootContext

// region context-first-look
// A key names a value and fixes its type. Create it once and reuse it.
private val REQUEST_ID: ContextKey<String> = createContextKey("request.id")

fun contextFirstLook() {
    val root = rootContext() // the empty context
    val withId = root.set(REQUEST_ID, "req-42") // a new context. root is untouched.

    println(root.get(REQUEST_ID)) // null
    println(withId.get(REQUEST_ID)) // req-42
}
// end-region
