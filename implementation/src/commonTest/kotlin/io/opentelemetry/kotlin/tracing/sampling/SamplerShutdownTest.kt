package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SamplerShutdownTest {

    @Test
    fun testDefaultShutdownSucceeds() = runTest {
        val sampler = object : Sampler {
            override fun shouldSample(
                context: Context,
                traceIdBytes: ByteArray,
                name: String,
                spanKind: SpanKind,
                attributes: AttributeContainer,
                links: List<SpanLink>,
            ): SamplingResult = error("not called")

            override val description: String = "NoShutdownOverride"
        }
        assertEquals(OperationResultCode.Success, sampler.shutdown())
    }

    @Test
    fun testParentBasedShutsDownEachDistinctDelegateOnce() = runTest {
        val root = FakeSampler()
        val shared = FakeSampler()
        val other = FakeSampler()
        val sampler = ParentBasedSampler(
            root = root,
            remoteParentSampled = shared,
            remoteParentNotSampled = other,
            localParentSampled = shared,
            localParentNotSampled = other,
        )

        assertEquals(OperationResultCode.Success, sampler.shutdown())
        assertEquals(1, root.shutdownCount)
        assertEquals(1, shared.shutdownCount)
        assertEquals(1, other.shutdownCount)
    }

    @Test
    fun testParentBasedReportsDelegateFailure() = runTest {
        val failing = FakeSampler(shutdownCode = { OperationResultCode.Failure })
        val ok = FakeSampler()
        val sampler = ParentBasedSampler(failing, ok, ok, ok, ok)

        assertEquals(OperationResultCode.Failure, sampler.shutdown())
        assertEquals(1, ok.shutdownCount)
    }

    @Test
    fun testThrowingDelegateDoesNotStopOtherDelegates() = runTest {
        val throwing = FakeSampler(shutdownCode = { error("boom") })
        val ok = FakeSampler()
        val sampler = ParentBasedSampler(throwing, ok, ok, ok, ok)

        val exc = assertFailsWith<IllegalStateException> { sampler.shutdown() }
        assertEquals("boom", exc.message)
        assertEquals(1, ok.shutdownCount)
    }

    @Test
    fun testAlwaysRecordForwardsShutdown() = runTest {
        val root = FakeSampler(shutdownCode = { OperationResultCode.Failure })

        assertEquals(OperationResultCode.Failure, AlwaysRecordSampler(root).shutdown())
        assertEquals(1, root.shutdownCount)
    }

    @Test
    fun testPerOperationShutsDownDefaultAndOperationSamplers() = runTest {
        val default = FakeSampler()
        val opA = FakeSampler()
        val opB = FakeSampler()
        val sampler = PerOperationSampler(default, mapOf("a" to opA, "b" to opB, "c" to default))

        assertEquals(OperationResultCode.Success, sampler.shutdown())
        assertEquals(1, default.shutdownCount)
        assertEquals(1, opA.shutdownCount)
        assertEquals(1, opB.shutdownCount)
    }
}
