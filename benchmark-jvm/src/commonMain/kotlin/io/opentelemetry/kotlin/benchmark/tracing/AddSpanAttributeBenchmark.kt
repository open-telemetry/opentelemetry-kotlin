package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanAttributeFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class AddSpanAttributeBenchmark {

    private lateinit var fixture: AddSpanAttributeFixture

    @Setup
    fun setup() {
        fixture = AddSpanAttributeFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddAttribute() {
        fixture.execute()
    }
}
