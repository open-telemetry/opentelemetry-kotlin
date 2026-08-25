package io.opentelemetry.kotlin.export

import okio.BufferedSink
import okio.blackholeSink
import okio.buffer

abstract class JsonExporter(protected val sink: BufferedSink = blackholeSink().buffer())