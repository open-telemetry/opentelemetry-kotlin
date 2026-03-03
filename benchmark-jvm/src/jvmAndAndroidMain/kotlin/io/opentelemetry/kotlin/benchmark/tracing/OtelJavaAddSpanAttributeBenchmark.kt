package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaAddSpanAttributeFixture
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class OtelJavaAddSpanAttributeBenchmark {

    private lateinit var fixture: OtelJavaAddSpanAttributeFixture

    @Setup
    fun setup() {
        fixture = OtelJavaAddSpanAttributeFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddAttributeOtelJava() {
        fixture.execute()
    }
}
