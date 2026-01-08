package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaSpanEndFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaEndSpanBenchmark {

    private lateinit var fixture: OtelJavaSpanEndFixture

    @Setup
    fun setup() {
        fixture = OtelJavaSpanEndFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkEndSpanOtelJava() {
        fixture.execute()
    }
}
