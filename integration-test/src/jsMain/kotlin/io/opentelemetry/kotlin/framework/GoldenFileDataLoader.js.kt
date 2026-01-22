package io.opentelemetry.kotlin.framework

actual fun loadTestFixture(fixtureName: String): String {
    val fs = js("require('fs')")
    val path = js("require('path')")
    val fixturePath = path.resolve("kotlin/$fixtureName")
    return fs.readFileSync(fixturePath, "utf-8") as String
}
