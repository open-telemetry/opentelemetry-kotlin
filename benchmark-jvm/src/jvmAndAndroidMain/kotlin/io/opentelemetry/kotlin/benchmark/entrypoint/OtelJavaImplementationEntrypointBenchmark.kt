package io.kotlin.opentelemetry.benchmark.entrypoint

import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class OtelJavaImplementationEntrypointBenchmark {

    @Benchmark
    fun benchmarkEntrypointImplementationOtelJava() {
        createOtelJavaOpenTelemetry()
    }
}