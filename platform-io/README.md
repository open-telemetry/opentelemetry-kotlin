# platform-io

This module holds platform-specific I/O code that depends on okio or kotlinx-coroutines, such as
the platform file system and the dispatcher that telemetry export runs on.

It's kept separate from `platform-implementations` so that module stays dependency-free and
foundational modules can use its primitives without pulling okio or coroutines onto their
consumers' classpaths.
