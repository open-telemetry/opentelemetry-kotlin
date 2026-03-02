package io.kotlin.opentelemetry.benchmark.entrypoint

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class OtelJavaNoopEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointNoopOtelJava() {
        OtelJavaOpenTelemetry.noop()
    }
}