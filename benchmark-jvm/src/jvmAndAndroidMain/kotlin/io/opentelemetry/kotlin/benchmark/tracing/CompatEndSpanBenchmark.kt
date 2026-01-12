package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaSpanEndFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanEndFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
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
