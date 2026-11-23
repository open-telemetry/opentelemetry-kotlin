package io.opentelemetry.example.collector

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

private const val DEFAULT_PORT = 4318

fun main(args: Array<String>) {
    val headless = "--headless" in args
    val port = System.getenv("LOCAL_COLLECTOR_PORT")?.toIntOrNull() ?: DEFAULT_PORT
    val store = TelemetryStore()

    if (!isPortFree(port)) {
        System.err.println("Port $port is already in use.")
        exitProcess(1)
    }

    val server = startOtlpReceiver(
        port = port,
        onSpans = { spans ->
            store.addSpans(spans)
            spans.forEach {
                println("SPAN  ${it.serviceName} ${it.name} ${"%.2f".format(it.durationMs)}ms trace=${it.traceId}")
            }
        },
        onLogs = { logs ->
            store.addLogs(logs)
            logs.forEach { println("LOG   ${it.serviceName} [${it.severity}] ${it.body}") }
        },
    )
    println("Local collector listening on http://0.0.0.0:$port (POST /v1/traces, /v1/logs)")

    if (headless) {
        Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
        CountDownLatch(1).await()
    } else {
        application {
            Window(
                onCloseRequest = {
                    server.stop()
                    exitApplication()
                },
                title = "OTel Local Collector (:$port)",
                state = rememberWindowState(width = 1200.dp, height = 800.dp),
            ) {
                CollectorApp(store)
            }
        }
    }
}

private fun isPortFree(port: Int): Boolean = try {
    ServerSocket(port).close()
    true
} catch (_: IOException) {
    false
}
