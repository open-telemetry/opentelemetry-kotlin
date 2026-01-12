package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanEndFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class EndSpanBenchmark {

    private lateinit var fixture: SpanEndFixture

    @Setup
    fun setup() {
        fixture = SpanEndFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkEndSpan() {
        fixture.execute()
    }
}
