package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaSpanCreationFixture
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class OtelJavaCreateSimpleSpanBenchmark {

    private lateinit var fixture: OtelJavaSpanCreationFixture

    @Setup
    fun setup() {
        fixture = OtelJavaSpanCreationFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkSimpleSpanOtelJava() {
        fixture.execute()
    }
}
