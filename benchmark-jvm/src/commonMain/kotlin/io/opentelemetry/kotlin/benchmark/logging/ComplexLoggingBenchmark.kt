package io.opentelemetry.kotlin.benchmark.logging

import io.opentelemetry.kotlin.benchmark.fixtures.logging.ComplexLoggingFixture
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class ComplexLoggingBenchmark {

    private lateinit var fixture: ComplexLoggingFixture

    @Setup
    fun setup() {
        fixture = ComplexLoggingFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkComplexLog() {
        fixture.execute()
    }
}
