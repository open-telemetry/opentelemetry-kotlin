package io.opentelemetry.kotlin.benchmark.entrypoint

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class ImplementationEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointImplementation() {
        createOpenTelemetry()
    }
}
