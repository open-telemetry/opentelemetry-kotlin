package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.baggage.createBaggage

// region context-baggage
fun baggageBasics() {
    val baggage = createBaggage {
        put("user.id", "u-123")
        put("region", "eu-west-1", metadata = "owner=payments")
    }

    // Baggage is immutable: set() and remove() return a new Baggage.
    val updated = baggage
        .set("region", "us-east-1")
        .remove("user.id")

    println(baggage.getValue("region")) // eu-west-1 (the original is unchanged)
    println(updated.getValue("region")) // us-east-1
    println(updated.getValue("user.id")) // null

    baggage.asMap().forEach { (name, entry) ->
        println("$name=${entry.value} metadata='${entry.metadata.value}'")
    }
}
// end-region
