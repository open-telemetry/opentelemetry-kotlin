package io.opentelemetry.kotlin.docs.snippets.propagation

// region propagation-example-requests
// Stand-in for your HTTP client's request type.
class OutgoingRequest(val url: String) {
    private val headers = linkedMapOf<String, String>()

    fun setHeader(name: String, value: String) {
        headers[name] = value
    }

    fun headers(): Map<String, String> = headers
}

// Stand-in for your HTTP server's request type. A header can appear more than once.
class IncomingRequest(private val headers: Map<String, List<String>>) {

    fun headerNames(): Set<String> = headers.keys

    // HTTP header names are case-insensitive.
    fun headerValues(name: String): List<String> = headers.entries
        .filter { it.key.equals(name, ignoreCase = true) }
        .flatMap { it.value }
}
// end-region
