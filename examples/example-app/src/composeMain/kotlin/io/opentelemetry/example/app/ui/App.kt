package io.opentelemetry.example.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.opentelemetry.example.app.initializeOtelSdk
import io.opentelemetry.kotlin.tracing.Span
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val otel = initializeOtelSdk()

private val flushScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@Composable
fun App() {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        flushScope.launch { otel.forceFlush() }
    }
    MaterialTheme {
        var selectedTab by remember { mutableStateOf(0) }
        var activeSpan by remember { mutableStateOf<Span?>(null) }
        val tabs = listOf("Spans", "Logs")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> SpanScreen(
                    otel = otel,
                    activeSpan = activeSpan,
                    onActiveSpanChanged = { activeSpan = it }
                )
                1 -> LogScreen(otel)
            }
        }
    }
}
