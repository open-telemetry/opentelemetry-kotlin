package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaSpanCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class CompatCreateSimpleSpanBenchmark {

    private lateinit var fixture: SpanCreationFixture

    @Setup
    fun setup() {
        fixture = SpanCreationFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkCreateSpanCompat() {
        fixture.execute()
    }
}
