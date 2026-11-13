package io.opentelemetry.kotlin

import java.util.IdentityHashMap

private const val MAX_CAUSES = 3
private const val MAX_SUPPRESSED = 3
private const val MAX_FRAMES = 200
private const val MAX_MESSAGE_LENGTH = 1024

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
        current.message?.take(MAX_MESSAGE_LENGTH)
    } catch (_: Throwable) {
        className(current)
    }

    fun append(current: Throwable, indent: String, relation: String?) {
        val name = className(current)
        if (visited.containsKey(current)) {
            output.append(indent).append(relation.orEmpty())
                .append("[CIRCULAR REFERENCE: ").append(name).append("]\n")
            return
        }
        visited[current] = true

        val message = safeMessage(current)
        output.append(indent).append(relation.orEmpty()).append(name)
        if (message != null) output.append(": ").append(message)
        output.append('\n')

        val frames = try {
            current.stackTrace
        } catch (_: Throwable) {
            emptyArray()
        }
        val frameIndent = "$indent\t"
        val remaining = (MAX_FRAMES - frameCount).coerceAtLeast(0)
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

        val suppressed = try {
            current.suppressed
        } catch (_: Throwable) {
            emptyArray()
        }
        for (item in suppressed) {
            if (suppressedCount == MAX_SUPPRESSED) {
                output.append(frameIndent).append("... suppressed exceptions omitted\n")
                break
            }
            suppressedCount++
            append(item, frameIndent, "Suppressed: ")
        }

        val cause = try {
            current.cause
        } catch (_: Throwable) {
            null
        }
        if (cause != null) {
            if (causeCount == MAX_CAUSES) {
                output.append(indent).append("Caused by: ... causes omitted\n")
            } else {
                causeCount++
                append(cause, indent, "Caused by: ")
            }
        }
    }

    append(throwable, "", null)
    val type = try {
        throwable.exceptionType()
    } catch (_: Throwable) {
        className(throwable)
    }
    val message = safeMessage(throwable)
    return SafeExceptionAttributes(type, message, output.toString().trimEnd())
}
