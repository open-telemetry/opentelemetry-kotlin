# exporters-in-memory

This module contains in-memory implementations of the span/log record exporters. These are intended
for testing purposes only.

Both exporters expose snapshots of the collected telemetry through `exportedSpans` and
`exportedLogRecords`. Call `reset()` between tests to clear the collected telemetry and reuse the
exporter. Previously returned snapshots are unchanged. Resetting a shut-down exporter clears its
telemetry but does not allow it to export again.
