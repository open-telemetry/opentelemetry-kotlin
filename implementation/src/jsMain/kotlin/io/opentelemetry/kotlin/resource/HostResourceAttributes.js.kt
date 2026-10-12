package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes

internal actual fun detectHostResourceAttributes(): HostResourceAttributes {
    return detectNodeHostResourceAttributes() ?: HostResourceAttributes()
}

private fun detectNodeHostResourceAttributes(): HostResourceAttributes? {
    return nodePlatform()?.let { platform ->
        HostResourceAttributes(
            osType = NodeOsTypeMapper.map(platform),
            osName = nodeOsName(),
            osVersion = nodeOsVersion(),
        )
    }
}

@OptIn(IncubatingApi::class)
internal object NodeOsTypeMapper {

    fun map(platform: String): String? = when (platform.lowercase()) {
        "aix" -> OsAttributes.OsTypeValues.AIX.value
        "android", "linux" -> OsAttributes.OsTypeValues.LINUX.value
        "darwin" -> OsAttributes.OsTypeValues.DARWIN.value
        "freebsd" -> OsAttributes.OsTypeValues.FREEBSD.value
        "netbsd" -> OsAttributes.OsTypeValues.NETBSD.value
        "openbsd" -> OsAttributes.OsTypeValues.OPENBSD.value
        "sunos" -> OsAttributes.OsTypeValues.SOLARIS.value
        "win32" -> OsAttributes.OsTypeValues.WINDOWS.value
        else -> null
    }
}

@Suppress("UnsafeCastFromDynamic")
private fun nodePlatform(): String? =
    js("typeof process !== 'undefined' && process.versions && process.versions.node ? process.platform : null")

@Suppress("UnsafeCastFromDynamic")
private fun nodeOsName(): String? = js(
    "typeof require === 'function' ? require('os').type() : null"
)

@Suppress("UnsafeCastFromDynamic")
private fun nodeOsVersion(): String? = js(
    "typeof require === 'function' ? require('os').release() : null"
)
