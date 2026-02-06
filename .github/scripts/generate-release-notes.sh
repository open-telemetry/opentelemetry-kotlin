#!/bin/bash -e

# Generates release notes, like what appears in GitHub release pages.

[[ -z "$1" ]] && { echo "Error: missing VERSION argument"; exit 1; }
[[ -z "$2" ]] && { echo "Error: missing PRIOR_VERSION argument"; exit 1; }
[[ ! -f "CHANGELOG.md" ]] && { echo "Error: CHANGELOG.md not found"; exit 1; }

VERSION=$1
PRIOR_VERSION=$2

sed -n "0,/^## Version $VERSION /d;/^## Version /q;p" CHANGELOG.md > /tmp/CHANGELOG_SECTION.md

# the complex perl regex is needed because markdown docs render newlines as soft wraps
# while release notes render them as line breaks
perl -0pe 's/(?<!\n)\n *(?!\n)(?![-*] )(?![1-9]+\. )/ /g' /tmp/CHANGELOG_SECTION.md \
    >> /tmp/release-notes.txt

cat >> /tmp/release-notes.txt << EOF
 ### 🙇 Thank you

 This release was possible thanks to the following contributors:

EOF

.github/scripts/generate-release-contributors.sh "v${PRIOR_VERSION}" >> /tmp/release-notes.txt
