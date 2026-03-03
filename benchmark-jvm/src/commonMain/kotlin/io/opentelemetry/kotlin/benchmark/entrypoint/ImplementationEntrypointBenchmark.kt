package io.opentelemetry.kotlin.benchmark.entrypoint

import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class ImplementationEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointImplementation() {
        createOpenTelemetry()
    }
}
