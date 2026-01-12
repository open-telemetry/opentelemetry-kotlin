package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
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
