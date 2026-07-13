# semconv

This module provides Kotlin representations of OTel semantic conventions. It generates them by downloading the
federated [End Use Client semantic conventions](https://github.com/bidetofevil/semantic-conventions-end-user-client)
registry from Github (which includes the core OTel semantic conventions) and using
[OTel weaver](https://github.com/open-telemetry/weaver) to generate Kotlin source files.
This workflow can be invoked by running `./gradlew generateSemanticConventions`.
