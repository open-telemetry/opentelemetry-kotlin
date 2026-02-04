#!/bin/bash
set -euo pipefail

# Builds the project including tests and static analysis

./gradlew build :benchmark-android:assemble :benchmark-jvm:assemble koverXmlReport --stacktrace
