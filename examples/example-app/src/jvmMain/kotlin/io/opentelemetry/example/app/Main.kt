package io.opentelemetry.example.app

import androidx.compose.ui.window.singleWindowApplication
import io.opentelemetry.example.app.ui.App

fun main() = singleWindowApplication(title = "OTel Example App") {
    App()
}
