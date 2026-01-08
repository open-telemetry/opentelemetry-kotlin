package io.opentelemetry.kotlin.benchmark.entrypoint

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createNoopOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalApi::class)
@RunWith(AndroidJUnit4::class)
class EntrypointBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmarkEntrypointNoop() {
        benchmarkRule.measureRepeated {
            createNoopOpenTelemetry()
        }
    }

    @Test
    fun benchmarkEntrypointImplementation() {
        benchmarkRule.measureRepeated {
            createOpenTelemetry()
        }
    }
}
