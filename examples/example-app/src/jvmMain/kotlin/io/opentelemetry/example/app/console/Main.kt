package io.opentelemetry.example.app.console

import io.opentelemetry.example.app.runAllExamples
import kotlinx.coroutines.runBlocking

/**
 * Default address of the collector in `examples/local-collector`.
 */
private const val LOCAL_COLLECTOR_URL = "http://localhost:4318"

/**
 * JVM console application entry point. Exports telemetry to the local collector by default.
 * Run `./gradlew :examples:local-collector:run` first to view it.
 */
fun main() = runBlocking {
    runAllExamples("JVM", url = LOCAL_COLLECTOR_URL)
}
