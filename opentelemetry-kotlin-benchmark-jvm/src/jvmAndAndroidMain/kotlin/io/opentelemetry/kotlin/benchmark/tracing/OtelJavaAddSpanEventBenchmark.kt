package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaAddSpanEventFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaAddSpanEventBenchmark {

    private lateinit var fixture: OtelJavaAddSpanEventFixture

    @Setup
    fun setup() {
        fixture = OtelJavaAddSpanEventFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddEventOtelJava() {
        fixture.execute()
    }
}
