package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.ReentrantReadWriteLock
import kotlin.reflect.KClass

/**
 * Limits only the default platform logger; installed [SdkErrorHandler]s are unaffected.
 *
 * A bounded, single-lock table avoids per-report key construction. It also caches the most recent
 * Throwable reference per key, so reporting the same Throwable avoids expensive stack inspection.
 * Distinct instances require extracting their top frame to avoid merging unrelated call sites.
 */
internal class DefaultErrorLogLimiter(
    private val maxKeys: Int = 64,
    private val frameExtractor: (Throwable) -> String? = { it.topFrameForLogDeduplication() },
) {
    init {
        require(maxKeys > 0)
    }

    internal sealed interface Decision {
        data object Full : Decision
        data object Skip : Decision
        data class Repeated(val count: Long) : Decision
        data class Overflow(val count: Long) : Decision
    }

    private class Entry(
        val kind: Int,
        val details: String,
        val api: String?,
        val severity: SdkErrorSeverity,
        val exceptionClass: KClass<out Throwable>?,
        val frame: String?,
        var lastCause: Throwable?,
    ) {
        var repeats: Long = 0

        fun matchesShape(kind: Int, details: String, api: String?): Boolean =
            this.kind == kind && this.details == details && this.api == api

        fun matchesType(
            severity: SdkErrorSeverity,
            exceptionClass: KClass<out Throwable>?,
        ): Boolean = this.severity == severity && this.exceptionClass == exceptionClass

        fun recordRepeat(): Decision {
            if (repeats < Long.MAX_VALUE) {
                repeats++
            }
            return if (repeats.isPowerOfTwo()) {
                Decision.Repeated(repeats)
            } else {
                Decision.Skip
            }
        }
    }

    private val lock = ReentrantReadWriteLock()
    private val entries = ArrayList<Entry>(maxKeys)
    private var overflows = 0L

    internal fun record(error: SdkError): Decision {
        val kind: Int
        val cause: Throwable?
        val api: String?
        when (error) {
            is SdkError.ApiMisuse -> {
                kind = 0
                cause = null
                api = error.api
            }
            is SdkError.UserCodeError -> {
                kind = 1
                cause = error.cause
                api = null
            }
            is SdkError.SdkCodeError -> {
                kind = 2
                cause = error.cause
                api = null
            }
        }
        val details = error.message
        val exceptionClass = cause?.let { it::class }
        val severity = error.severity

        return lock.write {
            // Fast path: a repeatedly reported Throwable needs no stack frame or key object.
            var i = 0
            while (i < entries.size) {
                val entry = entries[i]
                if (entry.matchesShape(kind, details, api) &&
                    entry.matchesType(severity, exceptionClass) && entry.lastCause === cause
                ) {
                    return@write entry.recordRepeat()
                }
                i++
            }

            // New throwable instance: the frame must be examined to distinguish call sites.
            val frame = try {
                cause?.let(frameExtractor)
            } catch (_: Throwable) {
                null
            }
            i = 0
            while (i < entries.size) {
                val entry = entries[i]
                if (entry.matchesShape(kind, details, api) &&
                    entry.matchesType(severity, exceptionClass) && entry.frame == frame
                ) {
                    entry.lastCause = cause
                    return@write entry.recordRepeat()
                }
                i++
            }

            if (entries.size >= maxKeys) {
                if (overflows < Long.MAX_VALUE) {
                    overflows++
                }
                return@write if (overflows.isPowerOfTwo()) {
                    Decision.Overflow(overflows)
                } else {
                    Decision.Skip
                }
            }

            entries.add(Entry(kind, details, api, severity, exceptionClass, frame, cause))
            Decision.Full
        }
    }
}

private fun Long.isPowerOfTwo(): Boolean = this > 0 && (this and (this - 1)) == 0L
