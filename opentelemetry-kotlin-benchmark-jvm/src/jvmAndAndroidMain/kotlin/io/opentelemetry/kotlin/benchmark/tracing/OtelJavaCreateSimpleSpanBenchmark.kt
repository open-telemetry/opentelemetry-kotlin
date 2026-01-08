package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaSpanCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
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
