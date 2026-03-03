package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanAttributeFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaAddSpanAttributeFixture
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class CompatAddSpanAttributeBenchmark {

    private lateinit var fixture: AddSpanAttributeFixture

    @Setup
    fun setup() {
        fixture = AddSpanAttributeFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddAttributeCompat() {
        fixture.execute()
    }
}
