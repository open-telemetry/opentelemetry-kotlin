package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.TracerCreationFixture
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class TracerCreationBenchmark {


    private lateinit var fixture: TracerCreationFixture

    @Setup
    fun setup() {
        fixture = TracerCreationFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkTracerCreation() {
        fixture.execute()
    }
}
