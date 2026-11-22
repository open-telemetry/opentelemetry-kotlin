package io.opentelemetry.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

public actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
