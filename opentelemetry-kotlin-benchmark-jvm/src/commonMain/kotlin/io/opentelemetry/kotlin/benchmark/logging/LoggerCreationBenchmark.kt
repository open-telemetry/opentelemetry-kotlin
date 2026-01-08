package io.opentelemetry.kotlin.benchmark.logging

import io.opentelemetry.kotlin.benchmark.fixtures.logging.LoggerCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class LoggerCreationBenchmark {

    private lateinit var fixture: LoggerCreationFixture

    @Setup
    fun setup() {
        fixture = LoggerCreationFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkLoggerCreation() {
        fixture.execute()
    }
}
