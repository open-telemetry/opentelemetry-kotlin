package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanEventFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaAddSpanEventFixture
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class CompatAddSpanEventBenchmark {

    private lateinit var fixture: AddSpanEventFixture

    @Setup
    fun setup() {
        fixture = AddSpanEventFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddEventCompat() {
        fixture.execute()
    }
}
