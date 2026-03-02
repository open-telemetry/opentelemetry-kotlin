package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanCreationFixture
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class CreateSimpleSpanBenchmark {

    private lateinit var fixture: SpanCreationFixture

    @Setup
    fun setup() {
        fixture = SpanCreationFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkCreateSpan() {
        fixture.execute()
    }
}
