package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaAddSpanLinkFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaAddSpanLinkBenchmark {

    private lateinit var fixture: OtelJavaAddSpanLinkFixture

    @Setup
    fun setup() {
        fixture = OtelJavaAddSpanLinkFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddLinkOtelJava() {
        fixture.execute()
    }
}
