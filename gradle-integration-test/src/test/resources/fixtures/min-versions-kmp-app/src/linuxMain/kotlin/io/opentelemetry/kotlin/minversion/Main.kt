@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.minversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.export.inMemorySpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor

fun main() {
    val api = createOpenTelemetry {
        tracerProvider {
            export {
                simpleSpanProcessor(inMemorySpanExporter())
            }
        }
    }
    api.tracerProvider.getTracer("linux-consumer").startSpan("test").end()
}
