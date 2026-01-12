package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.logging.OtelJavaLoggerCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaLoggerCreationBenchmark {

    private lateinit var fixture: OtelJavaLoggerCreationFixture

    @Setup
    fun setup() {
        fixture = OtelJavaLoggerCreationFixture(createOtelJavaOpenTelemetry())
    }

    @Benchmark
    fun benchmarkLoggerCreationOtelJava() {
        fixture.execute()
    }
}
