package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.logging.OtelJavaSimpleLoggingFixture
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class OtelJavaSimpleLoggingBenchmark {

    private lateinit var fixture: OtelJavaSimpleLoggingFixture

    @Setup
    fun setup() {
        fixture = OtelJavaSimpleLoggingFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkSimpleLogOtelJava() {
        fixture.execute()
    }
}
