package io.opentelemetry.example.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import io.opentelemetry.example.app.initializeOtelSdk
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.Span

@OptIn(ExperimentalApi::class)
private val otel = initializeOtelSdk()

@OptIn(ExperimentalApi::class)
@Composable
fun App() {
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
