package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaTracerCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.TracerCreationFixture
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class CompatTracerCreationBenchmark {

    private lateinit var fixture: TracerCreationFixture

    @Setup
    fun setup() {
        fixture = TracerCreationFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkTracerCreationCompat() {
        fixture.execute()
    }
}
