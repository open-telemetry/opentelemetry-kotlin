package io.opentelemetry.kotlin

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

public actual class ReentrantReadWriteLock {

    // visible to allow inlining
    public val impl: ReentrantReadWriteLock = ReentrantReadWriteLock()

    public actual inline fun <T> write(action: () -> T): T = impl.write { action() }

    public actual inline fun <T> read(action: () -> T): T = impl.read { action() }
}
