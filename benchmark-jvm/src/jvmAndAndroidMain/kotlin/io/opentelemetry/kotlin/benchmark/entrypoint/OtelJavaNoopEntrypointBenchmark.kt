package io.kotlin.opentelemetry.benchmark.entrypoint

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class OtelJavaNoopEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointNoopOtelJava() {
        OtelJavaOpenTelemetry.noop()
    }
}