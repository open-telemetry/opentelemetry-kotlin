package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.ComplexSpanCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class CreateComplexSpanBenchmark {

    private lateinit var fixture: ComplexSpanCreationFixture

    @Setup
    fun setup() {
        fixture = ComplexSpanCreationFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkCreateComplexSpan() {
        fixture.execute()
    }
}
