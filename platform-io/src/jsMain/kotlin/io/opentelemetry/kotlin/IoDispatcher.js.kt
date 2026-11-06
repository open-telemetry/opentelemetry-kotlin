package io.opentelemetry.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("InjectDispatcher")
public actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
