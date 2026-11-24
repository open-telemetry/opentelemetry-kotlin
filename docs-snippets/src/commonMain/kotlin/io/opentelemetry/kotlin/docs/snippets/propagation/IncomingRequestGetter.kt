package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.propagation.TextMapGetter

// region propagation-getter
object IncomingRequestGetter : TextMapGetter<IncomingRequest> {

    override fun keys(carrier: IncomingRequest): Collection<String> = carrier.headerNames()

    override fun get(carrier: IncomingRequest, key: String): String? =
        carrier.headerValues(key).firstOrNull()

    override fun getAll(carrier: IncomingRequest, key: String): List<String> =
        carrier.headerValues(key)
}
// end-region
