package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.benchmark.fixtures.logging.LoggerCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class CompatLoggerCreationBenchmark {

    private lateinit var fixture: LoggerCreationFixture

    @Setup
    fun setup() {
        fixture = LoggerCreationFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkLoggerCreationCompat() {
        fixture.execute()
    }
}
