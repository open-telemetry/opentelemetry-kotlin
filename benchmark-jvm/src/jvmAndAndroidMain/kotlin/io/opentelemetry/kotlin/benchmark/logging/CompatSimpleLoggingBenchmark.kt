package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.benchmark.fixtures.logging.SimpleLoggingFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class CompatSimpleLoggingBenchmark {

    private lateinit var fixture: SimpleLoggingFixture

    @Setup
    fun setup() {
        fixture = SimpleLoggingFixture(createCompatOpenTelemetry())
    }

    @Benchmark
    fun benchmarkSimpleLogCompat() {
        fixture.execute()
    }
}
