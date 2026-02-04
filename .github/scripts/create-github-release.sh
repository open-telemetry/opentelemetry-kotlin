#!/bin/bash
set -euo pipefail

# Creates a GitHub release using the gh CLI

if [[ -z "${VERSION:-}" ]]; then
  echo "Error: VERSION environment variable is required" >&2
  exit 1
fi

if [[ -z "${TARGET_REF:-}" ]]; then
  echo "Error: TARGET_REF environment variable is required" >&2
  exit 1
fi

notes_file="${NOTES_FILE:-/tmp/release-notes.txt}"

if [[ ! -f "$notes_file" ]]; then
  echo "Error: release notes file not found: $notes_file" >&2
  exit 1
fi

gh release create --target "$TARGET_REF" \
                  --title "Version $VERSION" \
                  --notes-file "$notes_file" \
                  "v$VERSION"

echo "Created GitHub release v$VERSION"
