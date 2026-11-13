#!/bin/bash -e

# Sets the version shown in the docs' install snippets, then re-syncs the snippets into the markdown.

[[ -z "$1" ]] && { echo "Error: missing version argument"; exit 1; }
[[ ! -f "gradle.properties" ]] && { echo "Error: gradle.properties not found"; exit 1; }

version=$1

sed -Ei "s/^io\.opentelemetry\.kotlin\.latestReleaseVersion=.*/io.opentelemetry.kotlin.latestReleaseVersion=$version/" gradle.properties
./gradlew syncDocSnippets --warn
