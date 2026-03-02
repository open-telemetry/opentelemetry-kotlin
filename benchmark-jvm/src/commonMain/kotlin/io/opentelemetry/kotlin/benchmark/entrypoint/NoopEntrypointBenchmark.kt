package io.opentelemetry.kotlin.benchmark.entrypoint

import io.opentelemetry.kotlin.NoopOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class NoopEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointNoop() {
        NoopOpenTelemetry
    }
}
