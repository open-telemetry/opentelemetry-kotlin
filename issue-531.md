# ConfigProvider: wire up and implement the Instrumentation Configuration API

## Context

The OTel [Instrumentation Configuration API](https://opentelemetry.io/docs/specs/otel/configuration/api/)
lets instrumentation libraries read their own configuration out of the declarative config file at
initialization time. It has two parts:

- `ConfigProvider` — the entry point; exposes the `.instrumentation` mapping node.
- `ConfigProperties` — a generic, programmatic view of a YAML mapping node (scalars, nested
  mappings, sequences, and the set of keys present). If `.instrumentation` is absent, the provider
  must return an **empty** `ConfigProperties`, never null.

Both interfaces already exist in `api` (commit `bbe54781`, "config provider (#598)"), along with
`internal object` no-ops in `noop`. But they are **dead code**: nothing references `ConfigProvider`
outside its own declaration, the two no-ops, and `NoopTests`. There is no way for an instrumentation
library to obtain one, and no implementation that actually reads YAML.

This change makes the API real:

1. `configProvider` becomes a member of the `OpenTelemetry` interface, so instrumentation can reach it.
2. `implementation` gets a working provider backed by the `.instrumentation` node of the config file.
3. `test-fakes` gets a controllable fake; `compat` gets a no-op (opentelemetry-java has not shipped
   this API in a module we depend on — see "compat" below).
4. Declarative config file reading is extended from JVM-only to Android and Apple, so the new
   provider is actually populated on mobile targets.

## Current pipeline (what exists)

```
createOpenTelemetry(config) / createCompatOpenTelemetry(config)
  └─ defaultBehaviorReader / defaultCompatBehaviorReader
       └─ config/OpenTelemetryConfigReader.read(dsl, configFilePath): OpenTelemetryBehavior
            ├─ envars            → OpenTelemetryEnvVars.toBehavior()
            ├─ declarative file  → platformDeclarativeConfigReader()?.read(path)
            │                       jvmMain: ConfigFileReaderImpl().read(path).toBehavior()
            │                       androidMain / appleMain / jsMain: null
            └─ dsl
          resolved by BehaviorResolverImpl → OpenTelemetryBehavior
  └─ OpenTelemetryImpl(...) / CompatOpenTelemetryImpl(...)
```

The critical gap: `DeclarativeConfigReader.read(path)` returns `OpenTelemetryBehavior`, so the
parsed document is discarded inside `PlatformDeclarativeConfigReader.jvm.kt` and the
`.instrumentation` node never escapes.

## Design decisions

**Carry the node on `OpenTelemetryBehavior`.** Add `instrumentation: Map<String, Any?>? = null` to
`behavior/.../OpenTelemetryBehavior.kt` and merge it with the existing `mergeMap` helper
(`behavior/.../BehaviorMerge.kt`). This requires **no signature change** anywhere in the read
pipeline — `DeclarativeConfigReader`, `OpenTelemetryConfigReader`, `BehaviorReader` and
`CompatBehaviorReader` all keep returning `OpenTelemetryBehavior`, and both entry points already
have the resolved behavior in hand when they construct their `OpenTelemetry` impl.

Store the **raw map**, not a `ConfigProperties`, because `OpenTelemetryBehavior` is a `data class`
and an interface-typed field would make its `equals`/`hashCode` depend on implementation identity.
`ConfigProperties` is built at the entry point instead.

**Use the raw YAML, not the typed schema model.** The generated
`OpenTelemetryConfiguration.instrumentationDevelopment` is bound to `@SerialName("instrumentation/development")`,
and `ExperimentalLanguageSpecificInstrumentation` is a `Map<String, @Contextual Any?>` that will not
deserialize under plain yamlkt. `ConfigProperties` also needs open-ended tree access
(`getStructured`, `propertyKeys`) that a typed data class cannot provide. The already-present but
currently **unused** `config-yaml/.../YamlConfigParser.kt` (`parse(yaml): Any?` → yamlkt
`Map<String, Any?>` / `List<*>` / scalars) is the right source.

Per the decision taken, read the node under the key **`instrumentation`** only.

**Placement.** The `Map`-backed `ConfigProperties` goes in `config-yaml`
(`io.opentelemetry.kotlin.config.yaml`), next to `YamlConfigParser`, which is where the raw
representation is produced. That module already depends on `api` transitively via `behavior`, and
has `containsPublicApi=false` so it needs no `.api` dump.

These declarations must be **public, not `internal`** — `config` and `implementation` are separate
Gradle modules and Kotlin `internal` does not cross module boundaries. Because the module sets
`containsPublicApi=false`, public here adds no published ABI surface. This follows the existing
precedent in the module: `YamlConfigParser` and `ConfigFileReaderImpl` are both declared with no
visibility modifier for the same reason.

## Implementation steps

### 1. Pin down yamlkt's scalar types (do this first) — DONE, findings below

`AGENTS.md` forbids inspecting the Gradle cache or decompiling jars, so the Kotlin types yamlkt's
`Yaml.decodeAnyFromString` produces were established empirically by characterization tests added to
`config-yaml/src/commonTest/.../YamlConfigParserTest.kt`. **Verified results:**

| YAML | Kotlin type |
|---|---|
| `n: 42` | **`Int`** — *not* `Long` |
| `d: 42.5` | `Double` |
| `b: true` | `Boolean` |
| `s: "42"` | `String` (stays a string, not coerced) |
| `nil: null` | key **present** in the map with a `null` value |
| `list: [1, 2, 3]` | `ArrayList<Int>` |
| sequence of mappings | `ArrayList<LinkedHashMap<String, Int>>` |

The `Int` result is the load-bearing one: an assertion written as `42L` failed with
`Integer<42>` vs `Long<42>`. This is why the coercion in step 2 matches on `Number` broadly
(`value.toLong()` / `value.toDouble()`) instead of on `Long`/`Double`, and also accepts a parseable
`String` — so it keeps working if a yamlkt upgrade changes the numeric subtype.

### 2. `MapConfigProperties` in `config-yaml`

New: `config-yaml/src/commonMain/kotlin/io/opentelemetry/kotlin/config/yaml/MapConfigProperties.kt`

```kotlin
class MapConfigProperties(private val node: Map<String, Any?>) : ConfigProperties
```

plus a `companion object { val EMPTY }` for the absent-node case, and a factory that turns a
`Map<String, Any?>?` into a non-null `ConfigProperties` (spec: absent ⇒ empty, never null):
`fun Map<String, Any?>?.toConfigProperties(): ConfigProperties`.

Accessor rules — **no accessor may ever throw** (AGENTS.md: never crash the host; invalid input
degrades to a safe default):

| Case | Result |
|---|---|
| key absent, or present with a null value | `null` |
| `getString` on a non-string scalar | the scalar's string form |
| `getLong` on `42` | `42` |
| `getLong` on `"42"` | `42` (parsed) |
| `getLong` on `42.9` | `42` (truncating) — document the choice |
| `getLong` on `"abc"` / a mapping / a list | `null` |
| `getBoolean` on `"true"`/`"false"` | parsed; anything else `null` |
| `getStructured` on a mapping | `MapConfigProperties` over it |
| `getStructured` on a non-mapping | `null` |
| `getStringList` on a heterogeneous list | coerce element-wise, **drop** elements that cannot convert |
| `getStructuredList` where an element is not a mapping | drop that element |
| `propertyKeys` | `node.keys` |

Note in the KDoc the spec's "present-but-null vs absent" requirement: the existing interface returns
`T?` uniformly, so `propertyKeys` is the only way to tell the two apart. Do **not** change the
interface for this — flag it as a known limitation and leave it to a follow-up.

### 3. Extract the instrumentation node when reading the file

`config-yaml` currently parses the file twice-over conceptually: `ConfigFileReaderImpl` returns the
typed `OpenTelemetryConfiguration` via `OpenTelemetryConfigurationParser`. The instrumentation node
needs the raw parse as well.

Add to `config-yaml`, alongside the existing `toBehavior()` mapper
(`OpenTelemetryConfigurationMapper.kt`), a function that reads the file's raw form with
`YamlConfigParser`, pulls out the `instrumentation` key if it is a mapping, and returns
`Map<String, Any?>?`. Then update the JVM actual (and the new Android/Apple ones from step 5) so the
`DeclarativeConfigReader` lambda produces a behavior carrying it:

```kotlin
DeclarativeConfigReader { path ->
    fileReader.read(path).toBehavior().copy(instrumentation = readInstrumentationNode(path))
}
```

Reading the file twice at init is acceptable (it happens once, at startup) and keeps the typed and
raw paths independent. If you prefer a single read, change `ConfigFileReader` to return both — but
that widens the diff.

### 4. Expose `configProvider` and implement it in all four implementors

- `api/src/commonMain/kotlin/io/opentelemetry/kotlin/OpenTelemetry.kt` — add
  `public val configProvider: ConfigProvider`, with KDoc linking the spec.
- `noop/.../NoopOpenTelemetryImpl.kt` — `override val configProvider = NoopConfigProvider`
  (already exists, same module, `internal` is fine).
- `test-fakes/.../FakeOpenTelemetry.kt` — `override val configProvider: ConfigProvider = FakeConfigProvider()`.
  New: `test-fakes/src/commonMain/kotlin/io/opentelemetry/kotlin/config/FakeConfigProvider.kt` and
  `FakeConfigProperties.kt`. Follow the module's style (`class FakeX`, no visibility modifier, public
  mutable state for assertions — see `FakeLoggerProvider`): let the test set `instrumentationConfig`,
  and back `FakeConfigProperties` with a mutable map.
- `implementation/.../OpenTelemetryImpl.kt` — new constructor param `override val configProvider: ConfigProvider`.
  New: `implementation/src/commonMain/kotlin/io/opentelemetry/kotlin/config/ConfigProviderImpl.kt`
  (`internal class ConfigProviderImpl(override val instrumentationConfig: ConfigProperties) : ConfigProvider`).
  In `OpenTelemetryImplEntrypoint.kt`, build it from `behavior.instrumentation` via the step-2
  factory and pass it in.
- `compat/.../CompatOpenTelemetryImpl.kt` — new constructor param, wired at **both** construction
  sites: `OpenTelemetryCompatEntrypoint.kt` (`createCompatOpenTelemetry`) and
  `OtelJavaOpenTelemetryExt.kt` (`toOtelKotlinApi`). Don't miss the second one.

**compat is a no-op.** opentelemetry-java's equivalent (`DeclarativeConfigProperties`) lives in
`opentelemetry-api-incubator`, which compat does not depend on, and `java-typealiases` has no alias
for it. `NoopConfigProvider` is `internal` to `noop`, so it is **not** reachable from compat even
though `compat → core → noop`. Add a compat-local stateless object instead, following the
`CompatResourceFactory` precedent:
`compat/src/jvmAndAndroidMain/kotlin/io/opentelemetry/kotlin/config/CompatConfigProvider.kt` —
an `internal object` returning an empty `ConfigProperties`. Add a `TODO`/KDoc noting it should
delegate once opentelemetry-java ships the API.

### 5. Enable declarative config on Android and Apple

`config-yaml` is already fully multiplatform (commonMain: yamlkt + okio + config-schema), and
`platform-implementations`' `getFileSystem()` returns a real `FileSystem.SYSTEM` on
`jvmAndAndroidMain` and `appleMain`. Only the wiring in `config` restricts it to the JVM.

- `config/build.gradle.kts` — move `implementation(project(":config-yaml"))` from `jvmMain` to `commonMain`.
- Replace the `return null` actuals in
  `config/src/androidMain/.../PlatformDeclarativeConfigReader.android.kt` and
  `config/src/appleMain/.../PlatformDeclarativeConfigReader.apple.kt` with the same body as the JVM
  actual. Consider collapsing jvm+android into a `jvmAndAndroidMain` actual if the module's source
  set layout allows it; otherwise duplicate.
- **Leave `jsMain` returning `null`.** `platform-implementations`' jsMain `getFileSystem()` throws
  `UnsupportedOperationException`, and `implementation/src/jsTest/.../CreateOpenTelemetryConfigFileTest.kt`
  asserts today that a declared config file is *accepted and ignored*. Returning a reader that throws
  only when a path is named would turn a silently-ignored setting into a hard init failure on a
  platform that cannot support it at all — that is not "misconfiguration", it is an unsupported
  platform, so silently ignoring is correct.
- Update the now-inaccurate KDoc on `PlatformDeclarativeConfigReader.kt` ("Currently declarative
  config files are only read on the JVM (not Android)") and on `defaultCompatBehaviorReader`
  ("Declarative configuration files are read on the JVM only").

### 6. `./gradlew apiDump`

`api/api/jvm/api.api` gains `getConfigProvider` on the `OpenTelemetry` block.

⚠️ `api/api/android/api.api` is **already stale** — its `OpenTelemetry` entry still reads
`: io/opentelemetry/kotlin/factory/SdkFactory` with only three members, and a `SdkFactory` type that
no longer exists in source. Regenerating will produce a large diff unrelated to this change. Inspect
it, and call it out in the PR description so reviewers don't think this change caused it.

`noop`, `implementation` and `compat` dumps only list public entry points, and all the new types are
`internal`, so those should not change. `test-fakes`, `behavior`, `config` and `config-yaml` have
`containsPublicApi=false` and no `.api` dir.

## Tests

| File | Cases |
|---|---|
| `config-yaml/src/commonTest/.../YamlConfigParserTest.kt` (extend) | step-1 characterization: int/double/bool/string/null scalars, scalar sequence, sequence of mappings |
| `config-yaml/src/commonTest/.../MapConfigPropertiesTest.kt` (new) | every row of the step-2 table; empty node ⇒ all accessors null + `propertyKeys` empty; nested `getStructured`; mistyped values return null rather than throwing |
| `behavior/src/commonTest/.../OpenTelemetryBehaviorTest.kt` | `instrumentation` survives a merge; higher layer wins on key collision; stays null when no layer set it |
| `config/src/jvmTest/.../PlatformDeclarativeConfigReaderTest.kt` (extend) | a config file with an `instrumentation:` node surfaces it on the behavior; a file without one leaves it null |
| `implementation/src/jvmTest/.../CreateOpenTelemetryConfigFileTest.kt` (extend) | **end-to-end**: `createOpenTelemetry { configFile(...) }` with an `instrumentation` node ⇒ `otel.configProvider.instrumentationConfig` returns the values; no node ⇒ empty, non-null provider. This file already has a `writeConfigFile` helper to reuse |
| `implementation/src/jsTest/.../CreateOpenTelemetryConfigFileTest.kt` | still passes unchanged (config file accepted and ignored) |
| `noop/src/commonTest/.../NoopTests.kt` | extend `testNoopConfigProvider()` to assert `NoopOpenTelemetry.configProvider` is reachable |
| `compat/src/jvmTest/.../` | `createCompatOpenTelemetry {}.configProvider.instrumentationConfig` is empty and non-null; likewise for `toOtelKotlinApi()` |

`kotlin.test` throughout, matching the surrounding style.

## Verification

⚠️ **`JAVA_HOME` is broken in this environment.** `~/.sdkman/candidates/java/current` points at
`21.0.12-tem`, which has a macOS `Home/` layout and no `bin/java`. Every Gradle invocation needs an
explicit JDK (pre-existing issue, unrelated to this change):

```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/21.0.5-amzn
```

```bash
./gradlew build                 # the real gate: tests + Detekt
./gradlew apiDump               # then review the api/ diffs, esp. the stale android one
./gradlew assembleAndroidTest   # Android compiles (don't run Android tests locally)
```

⚠️ **`./gradlew detekt` on its own proves nothing here.** Per
`buildSrc/src/main/kotlin/io/opentelemetry/kotlin/DetektConfig.kt`, the bare `detekt` task has no
source for KMP main source sets (it reports `NO-SOURCE`). Real static-analysis coverage comes from
`check`/`build`, which `dependsOn` every `Detekt` task plus a custom `detektCommonTest` task
registered over `src/commonTest/kotlin`. Gate on `./gradlew build`.

Apple targets matter here since step 5 newly enables file reading on them — `:<module>:build` does
cover `iosSimulatorArm64Test`, confirmed on `config-yaml`.

## Risks

- **Binary-breaking for external implementors.** Adding a member to the `OpenTelemetry` interface
  breaks anyone implementing it outside the repo. Everything is `@ExperimentalApi`, and the four
  in-repo implementors are all updated here, so this is acceptable — but it belongs in the changelog.
- **Behavior change on Android and Apple.** A previously-ignored `OTEL_CONFIG_FILE` env var or
  `configFile(...)` DSL call now takes effect on those platforms — and per
  `PlatformDeclarativeConfigReaderTest`, a missing or malformed file **throws at initialization**.
  An Android app that had a stale `OTEL_CONFIG_FILE` set, or that passes a path outside its sandbox,
  would start crashing at SDK init where it previously started fine. This is the riskiest part of the
  change; it is defensible under AGENTS.md ("fast fail should only happen when the SDK has been
  misconfigured at initialization") but is worth an explicit note in the PR and CHANGELOG, and is the
  one item that might be better split into its own PR if reviewers push back.
- **Double file read** at init (typed + raw). Startup-only, but noted in step 3.
- **PR size.** This will exceed the 500-line guideline in AGENTS.md. Per the decision taken it stays
  one PR; step 5 is the natural seam if it needs splitting later.
- **AGENTS.md** requires all AI-generated code to be manually reviewed before committing.
