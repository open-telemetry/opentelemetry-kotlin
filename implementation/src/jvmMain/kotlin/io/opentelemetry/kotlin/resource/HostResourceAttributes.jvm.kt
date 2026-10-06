package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes

internal actual fun detectHostResourceAttributes(): HostResourceAttributes {
    val osName = System.getProperty("os.name")

    return HostResourceAttributes(
        osType = JvmOsTypeMapper.map(osName),
        osName = osName,
        osVersion = System.getProperty("os.version"),
    )
}

@OptIn(IncubatingApi::class)
internal object JvmOsTypeMapper {

    fun map(osName: String?): String? = when {
        osName == null -> null
        osName.startsWith("Windows", ignoreCase = true) -> OsAttributes.OsTypeValues.WINDOWS.value
        osName.equals("Linux", ignoreCase = true) -> OsAttributes.OsTypeValues.LINUX.value
        osName.equals("Mac OS X", ignoreCase = true) ||
            osName.equals("Darwin", ignoreCase = true) -> OsAttributes.OsTypeValues.DARWIN.value
        osName.equals("FreeBSD", ignoreCase = true) -> OsAttributes.OsTypeValues.FREEBSD.value
        osName.equals("NetBSD", ignoreCase = true) -> OsAttributes.OsTypeValues.NETBSD.value
        osName.equals("OpenBSD", ignoreCase = true) -> OsAttributes.OsTypeValues.OPENBSD.value
        osName.equals("DragonFly BSD", ignoreCase = true) -> OsAttributes.OsTypeValues.DRAGONFLYBSD.value
        osName.equals("HP-UX", ignoreCase = true) -> OsAttributes.OsTypeValues.HPUX.value
        osName.equals("AIX", ignoreCase = true) -> OsAttributes.OsTypeValues.AIX.value
        osName.equals("SunOS", ignoreCase = true) -> OsAttributes.OsTypeValues.SOLARIS.value
        osName.equals("z/OS", ignoreCase = true) -> OsAttributes.OsTypeValues.ZOS.value
        else -> null
    }
}
