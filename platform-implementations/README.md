# platform-implementations

This module holds platform-specific code that isn't specific to a module. For example, defining
a function that gets the current time would require an `expect` declaration in `commonMain`, and
`actual` implementations in platform-specific sourceSets. Confining these functions here
encourages their reuse.

Please note that platform-specific code _can_ be specific to a module. For example, the
`compat` module makes use of JVM-specific code, which wouldn't be appropriate
to add here as there's no scope for reuse.
