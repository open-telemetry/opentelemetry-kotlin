package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext

// region context-current
private val ORDER_ID: ContextKey<String> = createContextKey("order.id")

fun handleOrder(orderId: String) {
    val ctx = currentContext().set(ORDER_ID, orderId)

    ctx.runWithContext {
        chargeCard() // no extra parameter needed
    }
    // The previous context is current again here.
}

private fun chargeCard() {
    val orderId = currentContext().get(ORDER_ID)
    println("charging card for order $orderId")
}
// end-region
