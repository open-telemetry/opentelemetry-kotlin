#!/bin/bash
set -euo pipefail

# Generates release notes

if [[ -z "${VERSION:-}" ]]; then
  echo "Error: VERSION environment variable is required" >&2
  exit 1
fi

if [[ -z "${PRIOR_VERSION:-}" ]]; then
  echo "Error: PRIOR_VERSION environment variable is required" >&2
  exit 1
fi

if [[ ! -f "CHANGELOG.md" ]]; then
  echo "Error: CHANGELOG.md not found" >&2
  exit 1
fi

sed -n "0,/^## Version $VERSION /d;/^## Version /q;p" CHANGELOG.md > /tmp/CHANGELOG_SECTION.md

# the complex perl regex is needed because markdown docs render newlines as soft wraps
# while release notes render them as line breaks
perl -0pe 's/(?<!\n)\n *(?!\n)(?![-*] )(?![1-9]+\. )/ /g' /tmp/CHANGELOG_SECTION.md \
    >> /tmp/release-notes.txt

cat >> /tmp/release-notes.txt << EOF
 ### 🙇 Thank you

 This release was possible thanks to the following contributors:

EOF

"$(dirname "$0")/generate-release-contributors.sh" "v${PRIOR_VERSION}" >> /tmp/release-notes.txt
