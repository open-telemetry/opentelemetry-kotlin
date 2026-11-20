package io.opentelemetry.example.app

/**
 * Address of `examples/local-collector` as seen from the current platform. The Android emulator
 * reaches the host machine via `10.0.2.2`, every other target via `localhost`.
 */
internal expect val defaultCollectorUrl: String
