package io.opentelemetry.kotlin.resource

import android.os.Build
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes

@OptIn(IncubatingApi::class)
internal actual fun detectHostResourceAttributes(): HostResourceAttributes = HostResourceAttributes(
    osType = OsAttributes.OsTypeValues.LINUX.value,
    osName = "Android",
    osVersion = Build.VERSION.RELEASE.takeIf(String::isNotBlank),
)
