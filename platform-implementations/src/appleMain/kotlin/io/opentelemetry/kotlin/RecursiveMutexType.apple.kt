package io.opentelemetry.kotlin

import platform.posix.PTHREAD_MUTEX_RECURSIVE

internal actual val recursiveMutexType: Int = PTHREAD_MUTEX_RECURSIVE
