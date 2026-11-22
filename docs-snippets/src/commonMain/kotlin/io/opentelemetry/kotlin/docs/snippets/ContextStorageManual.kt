package io.opentelemetry.kotlin.docs.snippets

import io.opentelemetry.kotlin.context.createThreadLocalCurrentContextStorage
import io.opentelemetry.kotlin.context.installContextStorage
import io.opentelemetry.kotlin.context.uninstallContextStorage

// region context-storage-manual
fun installStorage() {
    val storage = createThreadLocalCurrentContextStorage()
    installContextStorage(storage) // false if a storage is already installed
    // ...
    uninstallContextStorage(storage) // only removes the storage if it's this instance
}
// end-region
