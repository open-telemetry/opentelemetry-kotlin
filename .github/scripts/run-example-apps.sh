#!/bin/bash
set -euo pipefail

# Build and run the example apps

./gradlew :examples:jvm-app:run :examples:android-app:assembleRelease :examples:js-app:jsNodeDevelopmentRun --stacktrace
