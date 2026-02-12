package io.opentelemetry.kotlin.benchmark.entrypoint

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class NoopEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointNoop() {
        NoopOpenTelemetry
    }
}
