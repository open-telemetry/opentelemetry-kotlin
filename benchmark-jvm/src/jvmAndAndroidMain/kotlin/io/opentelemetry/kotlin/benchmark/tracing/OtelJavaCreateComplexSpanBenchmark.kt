package io.kotlin.opentelemetry.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.OtelJavaComplexSpanCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaCreateComplexSpanBenchmark {

    private lateinit var fixture: OtelJavaComplexSpanCreationFixture

    @Setup
    fun setup() {
        fixture = OtelJavaComplexSpanCreationFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkComplexSpanOtelJava() {
        fixture.execute()
    }
}
