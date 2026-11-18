package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.rootContext

// region context-immutability
private val TENANT: ContextKey<String> = createContextKey("tenant")
private val ATTEMPT: ContextKey<Int> = createContextKey("attempt")

fun contextImmutability() {
    val base = rootContext().set(TENANT, "acme")

    // set() always returns a new context, so calls can be chained.
    val first = base.set(ATTEMPT, 1)
    val second = first.set(ATTEMPT, 2) // replaces the value in the new context only

    println(first.get(ATTEMPT)) // 1
    println(second.get(ATTEMPT)) // 2
    println(second.get(TENANT)) // acme (inherited from base)

    // Setting a value to null removes it.
    val withoutTenant = second.set(TENANT, null)
    println(withoutTenant.get(TENANT)) // null
}
// end-region
