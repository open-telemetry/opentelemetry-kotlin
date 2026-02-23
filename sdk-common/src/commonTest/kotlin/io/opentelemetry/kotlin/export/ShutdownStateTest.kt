package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ShutdownStateTest {

    private lateinit var state: MutableShutdownState

    @BeforeTest
    fun setup() {
        state = MutableShutdownState()
    }

    @Test
    fun testInitialStateIsNotShutdown() {
        assertFalse(state.isShutdown)
    }

    @Test
    fun testShutdownTransitionsState() {
        state.shutdownNow()
        assertTrue(state.isShutdown)
    }

    @Test
    fun testIsShutdownRemainsTrue() {
        state.shutdownNow()
        assertTrue(state.isShutdown)
        state.shutdownNow()
        assertTrue(state.isShutdown)
    }

    @Test
    fun testIfActiveOrElseRunsActionWhenActive() {
        val result = state.ifActiveOrElse("default") { "active" }
        assertEquals("active", result)
    }

    @Test
    fun testIfActiveOrElseReturnsDefaultWhenShutdown() {
        state.shutdownNow()
        val result = state.ifActiveOrElse("default") { "active" }
        assertEquals("default", result)
    }

    @Test
    fun testExecuteRunsActionWhenActive() {
        var called = false
        state.execute { called = true }
        assertTrue(called)
    }

    @Test
    fun testExecuteSkipsActionWhenShutdown() {
        state.shutdownNow()
        var called = false
        state.execute { called = true }
        assertFalse(called)
    }

    @Test
    fun testIfActiveResultReturnsSuccessWhenActive() {
        val result = state.ifActive { Success }
        assertEquals(Success, result)
    }

    @Test
    fun testIfActiveResultReturnsFailureWhenShutdown() {
        state.shutdownNow()
        val result = state.ifActive { Success }
        assertEquals(Failure, result)
    }

    @Test
    fun testReadOnlyIfActiveOrElseRunsActionWhenActive() {
        val readOnly: ShutdownState = state
        val result = readOnly.ifActiveOrElse("default") { "active" }
        assertEquals("active", result)
    }

    @Test
    fun testReadOnlyIfActiveOrElseReturnsDefaultWhenShutdown() {
        val readOnly: ShutdownState = state
        state.shutdownNow()
        val result = readOnly.ifActiveOrElse("default") { "active" }
        assertEquals("default", result)
    }

    @Test
    fun testReadOnlyExecuteRunsActionWhenActive() {
        val readOnly: ShutdownState = state
        var called = false
        readOnly.execute { called = true }
        assertTrue(called)
    }

    @Test
    fun testReadOnlyExecuteSkipsActionWhenShutdown() {
        val readOnly: ShutdownState = state
        state.shutdownNow()
        var called = false
        readOnly.execute { called = true }
        assertFalse(called)
    }

    @Test
    fun testReadOnlyIfActiveReturnsSuccessWhenActive() {
        val readOnly: ShutdownState = state
        val result = readOnly.ifActive { Success }
        assertEquals(Success, result)
    }

    @Test
    fun testReadOnlyIfActiveReturnsFailureWhenShutdown() {
        val readOnly: ShutdownState = state
        state.shutdownNow()
        val result = readOnly.ifActive { Success }
        assertEquals(Failure, result)
    }

    @Test
    fun testShutdownWithActionRunsActionAndSetsFlag() {
        var actionCalled = false
        val result = state.shutdown {
            actionCalled = true
            Success
        }
        assertTrue(actionCalled)
        assertTrue(state.isShutdown)
        assertEquals(Success, result)
    }

    @Test
    fun testShutdownWithActionReturnsSuccessWhenAlreadyShutdown() {
        state.shutdownNow()
        var actionCalled = false
        val result = state.shutdown {
            actionCalled = true
            Failure
        }
        assertFalse(actionCalled)
        assertEquals(Success, result)
    }

    @Test
    fun testShutdownWithActionPropagatesFailure() {
        val result = state.shutdown { Failure }
        assertTrue(state.isShutdown)
        assertEquals(Failure, result)
    }
}
