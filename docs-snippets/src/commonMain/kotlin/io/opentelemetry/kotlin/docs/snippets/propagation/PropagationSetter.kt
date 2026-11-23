package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.propagation.TextMapSetter

// region propagation-setter
// TextMapSetter is a fun interface, so a lambda works.
val outgoingRequestSetter: TextMapSetter<OutgoingRequest> = TextMapSetter { request, key, value ->
    request.setHeader(key, value)
}
// end-region
