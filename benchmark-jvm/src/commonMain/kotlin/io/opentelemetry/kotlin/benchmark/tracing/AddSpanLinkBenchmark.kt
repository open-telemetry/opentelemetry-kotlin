package io.opentelemetry.kotlin.benchmark.tracing

import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanLinkFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class AddSpanLinkBenchmark {

    private lateinit var fixture: AddSpanLinkFixture

    @Setup
    fun setup() {
        fixture = AddSpanLinkFixture(createOpenTelemetry())
    }

    @Benchmark
    fun benchmarkAddLink() {
        fixture.execute()
    }
}
