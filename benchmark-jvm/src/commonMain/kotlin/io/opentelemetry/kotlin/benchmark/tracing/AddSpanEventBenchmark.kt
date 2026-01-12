package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanEventFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class AddSpanEventBenchmark {

    private lateinit var fixture: AddSpanEventFixture

    @Setup
    fun setup() {
        fixture = AddSpanEventFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddEvent() {
        fixture.execute()
    }
}
