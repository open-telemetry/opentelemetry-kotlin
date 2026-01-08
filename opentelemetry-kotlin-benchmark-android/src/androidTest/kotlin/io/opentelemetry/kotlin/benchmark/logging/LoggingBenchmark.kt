package io.opentelemetry.kotlin.benchmark.logging

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.opentelemetry.kotlin.benchmark.fixtures.logging.ComplexLoggingFixture
import io.opentelemetry.kotlin.benchmark.fixtures.logging.LoggerCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.logging.SimpleLoggingFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalApi::class)
@RunWith(AndroidJUnit4::class)
class LoggingBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmarkLoggerCreation() {
        val fixture = LoggerCreationFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkSimpleLog() {
        val fixture = SimpleLoggingFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkComplexLog() {
        val fixture = ComplexLoggingFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }
}
