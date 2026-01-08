package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.TracerCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
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
