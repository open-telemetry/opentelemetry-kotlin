package io.opentelemetry.kotlin

import kotlin.concurrent.Volatile

public actual fun getCurrentTimeNanos(): Long {
    // Use the anchored clock implementation for better precision
    return getAnchoredClockNanos()
}

/**
 * Anchored clock implementation that provides nanosecond precision by:
 * 1. Anchoring the epoch once using currentTimeMillis()
 * 2. Measuring elapsed time using System.nanoTime() (which has higher resolution)
 * 3. Periodically updating the anchor to prevent overflow
 * 
 * This pattern is used by opentelemetry-java for better timing precision.
 */
private object AnchoredClock {
    @Volatile
    private var anchorMillis: Long = 0L
    @Volatile
    private var anchorNanos: Long = 0L
    
    init {
        updateAnchor()
    }
    
    @Synchronized
    private fun updateAnchor() {
        anchorMillis = System.currentTimeMillis()
        anchorNanos = System.nanoTime()
    }
    
    fun getNanos(): Long {
        val currentNanos = System.nanoTime()
        val elapsedNanos = currentNanos - anchorNanos
        
        // Update anchor if we're getting close to overflow
        // System.nanoTime() overflows every ~292 years, so we update every ~100 years
        if (elapsedNanos > 3_155_760_000_000_000_000L) { // ~100 years in nanoseconds
            updateAnchor()
            return getNanos()
        }
        
        return (anchorMillis * 1_000_000L) + elapsedNanos
    }
}

private fun getAnchoredClockNanos(): Long = AnchoredClock.getNanos()
