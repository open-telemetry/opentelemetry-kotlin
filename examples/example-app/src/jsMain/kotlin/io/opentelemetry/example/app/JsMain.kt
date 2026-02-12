@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun main() {
    MainScope().launch {
        runAllExamples("JS")
    }
}
