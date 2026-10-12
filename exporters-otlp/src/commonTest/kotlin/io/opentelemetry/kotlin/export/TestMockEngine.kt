package io.opentelemetry.kotlin.export

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import kotlinx.coroutines.test.TestDispatcher

internal fun testMockEngine(dispatcher: TestDispatcher, handler: MockRequestHandler): MockEngine =
    MockEngine(
        MockEngineConfig().apply {
            this.dispatcher = dispatcher
            addHandler(handler)
        }
    )
