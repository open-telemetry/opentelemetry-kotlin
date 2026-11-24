package io.opentelemetry.kotlin

import java.util.IdentityHashMap

private const val MAX_CAUSES = 3
private const val MAX_SUPPRESSED = 3
private const val MAX_FRAMES = 200

public actual fun safeExceptionAttributes(throwable: Throwable): SafeExceptionAttributes {
    val visited = IdentityHashMap<Throwable, Boolean>()
    val output = StringBuilder()
    var causeCount = 0
    var suppressedCount = 0
    var frameCount = 0

    fun className(current: Throwable): String = try {
        current.javaClass.name
    } catch (_: Throwable) {
        "Throwable"
    }

    fun safeMessage(current: Throwable): String? = try {
        current.message?.let(::truncateUtf8)
    } catch (_: Throwable) {
        className(current)
    }

    fun append(
        current: Throwable,
        indent: String,
        relation: String?,
        knownMessage: String? = null,
        hasKnownMessage: Boolean = false,
    ) {
        val name = className(current)
        if (visited.containsKey(current)) {
            output.append(indent).append(relation.orEmpty())
                .append("[CIRCULAR REFERENCE: ").append(name).append("]\n")
            return
        }
        visited[current] = true

        val message = if (hasKnownMessage) knownMessage else safeMessage(current)
        output.append(indent).append(relation.orEmpty()).append(name)
        if (message != null) {
            output.append(": ").append(message)
        }
        output.append('\n')

        if (frameCount < MAX_FRAMES) {
            val frames = try {
                current.stackTrace
            } catch (_: Throwable) {
                emptyArray()
            }
            val frameIndent = "$indent\t"
            val remaining = MAX_FRAMES - frameCount
            val visibleFrames = minOf(frames.size, remaining)
            for (index in 0 until visibleFrames) {
                val frame = try {
                    frames[index].toString()
                } catch (_: Throwable) {
                    "<unavailable>"
                }
                output.append(frameIndent).append("at ").append(frame).append('\n')
            }
            frameCount += visibleFrames
            if (visibleFrames < frames.size) {
                output.append(frameIndent).append("... ")
                    .append(frames.size - visibleFrames).append(" frames omitted\n")
            }
        }

        if (suppressedCount < MAX_SUPPRESSED) {
            val frameIndent = "$indent\t"
            val suppressed = try {
                current.suppressed
            } catch (_: Throwable) {
                emptyArray()
            }
            for (item in suppressed) {
                if (suppressedCount >= MAX_SUPPRESSED) {
                    output.append(frameIndent).append("... suppressed exceptions omitted\n")
                    break
                }
                suppressedCount++
                append(item, frameIndent, "Suppressed: ")
            }
        }

        if (causeCount < MAX_CAUSES) {
            val cause = try {
                current.cause
            } catch (_: Throwable) {
                null
            }
            if (cause != null) {
                causeCount++
                append(cause, indent, "Caused by: ")
            }
        }
    }

    val message = safeMessage(throwable)
    append(throwable, "", null, knownMessage = message, hasKnownMessage = true)
    val type = try {
        throwable.exceptionType()
    } catch (_: Throwable) {
        className(throwable)
    }
    return SafeExceptionAttributes(type, message, output.toString().trimEnd())
}
