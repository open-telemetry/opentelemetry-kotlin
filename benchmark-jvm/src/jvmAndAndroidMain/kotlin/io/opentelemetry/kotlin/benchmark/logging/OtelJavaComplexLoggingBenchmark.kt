package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.logging.OtelJavaComplexLoggingFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaComplexLoggingBenchmark {

    private lateinit var fixture: OtelJavaComplexLoggingFixture

    @Setup
    fun setup() {
        fixture = OtelJavaComplexLoggingFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkComplexLogOtelJava() {
        fixture.execute()
    }
}
