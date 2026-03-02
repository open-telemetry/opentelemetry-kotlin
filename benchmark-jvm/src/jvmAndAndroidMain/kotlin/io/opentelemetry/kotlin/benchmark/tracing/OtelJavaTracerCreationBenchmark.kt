package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaTracerCreationFixture
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class OtelJavaTracerCreationBenchmark {

    private lateinit var fixture: OtelJavaTracerCreationFixture

    @Setup
    fun setup() {
        fixture = OtelJavaTracerCreationFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkTracerCreationOtelJava() {
        fixture.execute()
    }
}
