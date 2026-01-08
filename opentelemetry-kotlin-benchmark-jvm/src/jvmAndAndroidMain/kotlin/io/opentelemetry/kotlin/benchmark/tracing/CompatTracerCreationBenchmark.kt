package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaTracerCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.TracerCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
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
