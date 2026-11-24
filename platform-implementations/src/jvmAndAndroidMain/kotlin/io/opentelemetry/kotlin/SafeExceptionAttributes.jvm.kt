package io.opentelemetry.kotlin

import java.util.IdentityHashMap

private const val MAX_CAUSES = 3
private const val MAX_SUPPRESSED = 3
private const val MAX_FRAMES = 200

public actual fun safeExceptionAttributes(throwable: Throwable): SafeExceptionAttributes {
    val className = throwable.javaClass.name
    val type = try {
        throwable.exceptionType()
    } catch (_: Throwable) {
        className
    }
    val message = safeMessage(throwable, className)
    val stacktrace = try {
        buildString {
            StacktraceState().appendThrowable(
                this,
                throwable,
                knownMessage = message,
                hasKnownMessage = true,
            )
            append('\n')
        }
    } catch (_: Throwable) {
        "$className\n"
    }
    return SafeExceptionAttributes(type, message, stacktrace)
}

private class StacktraceState {
    private val seen = IdentityHashMap<Throwable, Unit>()
    private var causeCount = 0
    private var suppressedCount = 0
    private var frameCount = 0

    fun appendThrowable(
        output: StringBuilder,
        throwable: Throwable,
        caption: String = "",
        indent: String = "",
        knownMessage: String? = null,
        hasKnownMessage: Boolean = false,
    ) {
        val type = throwable.javaClass.name
        if (seen.put(throwable, Unit) != null) {
            output.append(indent).append(caption).append("[CIRCULAR REFERENCE: ").append(type).append(']')
            return
        }

        output.append(indent).append(caption).append(type)
        val message = when {
            hasKnownMessage -> knownMessage
            else -> safeMessage(throwable, type)
        }
        message?.takeIf { it.isNotEmpty() }?.let {
            output.append(": ").append(it)
        }

        if (frameCount < MAX_FRAMES) {
            for (frame in safeStackTrace(throwable)) {
                if (frameCount >= MAX_FRAMES) {
                    break
                }
                output.append('\n').append(indent).append("\tat ").append(safeFrame(frame))
                frameCount++
            }
        }

        if (suppressedCount < MAX_SUPPRESSED) {
            for (suppressed in safeSuppressed(throwable)) {
                if (suppressedCount >= MAX_SUPPRESSED) {
                    break
                }
                suppressedCount++
                output.append('\n')
                appendThrowable(output, suppressed, "Suppressed: ", "$indent\t")
            }
        }

        if (causeCount < MAX_CAUSES) {
            safeCause(throwable)?.let { cause ->
                causeCount++
                output.append('\n')
                appendThrowable(output, cause, "Caused by: ", indent)
            }
        }
    }
}

private fun safeMessage(throwable: Throwable, fallback: String): String? = try {
    throwable.message?.let(::truncateMessage)
} catch (_: Throwable) {
    fallback
}

private fun safeStackTrace(throwable: Throwable): Array<StackTraceElement> = try {
    throwable.stackTrace
} catch (_: Throwable) {
    emptyArray()
}

private fun safeSuppressed(throwable: Throwable): Array<Throwable> = try {
    throwable.suppressed
} catch (_: Throwable) {
    emptyArray()
}

private fun safeCause(throwable: Throwable): Throwable? = try {
    throwable.cause
} catch (_: Throwable) {
    null
}

private fun safeFrame(frame: StackTraceElement): String = try {
    frame.toString()
} catch (_: Throwable) {
    "<unknown>"
}

private fun truncateMessage(value: String): String = truncateUtf8(value)
