
package io.opentelemetry.example.app

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun main() {
    MainScope().launch {
        runAllExamples("JS")
    }
}
