package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaSpanEndFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanEndFixture
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class CompatEndSpanBenchmark {

    private lateinit var fixture: SpanEndFixture

    @Setup
    fun setup() {
        fixture = SpanEndFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkEndSpanCompat() {
        fixture.execute()
    }
}
