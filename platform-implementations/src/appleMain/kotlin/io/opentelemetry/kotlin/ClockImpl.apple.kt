package io.opentelemetry.kotlin

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

public actual fun getCurrentTimeNanos(): Long {
    // NSDate.timeIntervalSince1970 returns seconds since epoch as a Double
    // Convert to nanoseconds by multiplying by 1_000_000_000
    return (NSDate().timeIntervalSince1970 * 1_000_000_000.0).toLong()
}
