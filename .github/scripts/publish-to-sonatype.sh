#!/bin/bash
set -euo pipefail

# Publishes artifacts to Sonatype/Maven Central.

./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository \
  -Pfinal=true \
  --no-build-cache \
  --no-configuration-cache \
  --no-parallel
