package io.opentelemetry.api.baggage

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

internal val otelJavaBaggageContextKey: OtelJavaContextKey<Baggage> = BaggageContextKey.KEY
