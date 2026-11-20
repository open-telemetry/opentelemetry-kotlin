package io.opentelemetry.example.app.console

import io.opentelemetry.example.app.runAllExamples
import kotlinx.coroutines.runBlocking

/**
 * JVM console application entry point. Exports telemetry to `examples/local-collector`.
 * Run `./gradlew :examples:local-collector:run` first to view it.
 */
fun main() = runBlocking {
    runAllExamples("JVM")
}
