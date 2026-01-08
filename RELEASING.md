# Releasing opentelemetry-kotlin

## Checklist

1. Run `pre-release-workflow.yml` on GitHub Actions
2. Sign into Sonatype and ensure the artifacts have been validated
3. Publish the artifacts
4. Open a PR with a changelog entry that describes what changed in the new version
5. Create a release on GitHub's UI using the tag
6. Merge the version bump PR created by the CI bot
