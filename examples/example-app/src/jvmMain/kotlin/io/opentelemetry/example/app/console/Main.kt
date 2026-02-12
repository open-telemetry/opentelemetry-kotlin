@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.app.console

import io.opentelemetry.example.app.runAllExamples
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.runBlocking

/**
 * JVM console application entry point.
 */
fun main() = runBlocking {
    runAllExamples("JVM")
}
