package io.opentelemetry.kotlin.benchmark.logging

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.opentelemetry.kotlin.benchmark.createOtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.logging.OtelJavaComplexLoggingFixture
import io.opentelemetry.kotlin.benchmark.fixtures.logging.OtelJavaLoggerCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.logging.OtelJavaSimpleLoggingFixture
import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalApi::class)
@RunWith(AndroidJUnit4::class)
class OtelJavaLoggingBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmarkLoggerCreationOtelJava() {
        val fixture = OtelJavaLoggerCreationFixture(createOtelJavaOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkSimpleLogOtelJava() {
        val fixture = OtelJavaSimpleLoggingFixture(createOtelJavaOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkComplexLogOtelJava() {
        val fixture = OtelJavaComplexLoggingFixture(createOtelJavaOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }
}
