
package io.opentelemetry.example.app.console

import io.opentelemetry.example.app.runAllExamples
import kotlinx.coroutines.runBlocking

/**
 * JVM console application entry point.
 */
fun main() = runBlocking {
    runAllExamples("JVM")
}
