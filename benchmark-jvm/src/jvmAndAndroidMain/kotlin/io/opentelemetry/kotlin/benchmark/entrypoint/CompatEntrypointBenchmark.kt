package io.kotlin.opentelemetry.benchmark.entrypoint

import io.opentelemetry.kotlin.createCompatOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class CompatEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointCompat() {
        createCompatOpenTelemetry()
    }
}