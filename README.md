# json-to-view

**json-to-view** is an Android SDK that turns a declarative UI tree (JSON or Kotlin model) into on-screen layout using **either**:

| Mode | Class | When to use |
|------|--------|-------------|
| **Flat** | `FlatHostView` / `JsonToViewHost.Mode.FLAT` | Performance — one host, canvas draw, few Android `View`s |
| **Nested** | `NestedHost` / `JsonToViewHost.Mode.NESTED` | Debug / hierarchy inspection — real `ViewGroup` tree |

Same model (`FNode`) for both backends.

> Status: **0.1.0** — usable as a project dependency; Maven Central not published yet. Sample app included.

## Install

### Project module (this repo)

```kotlin
// settings.gradle.kts
include(":json-to-view")

// app/build.gradle.kts
implementation(project(":json-to-view"))
```

### mavenLocal (after `./gradlew :json-to-view:publishToMavenLocal`)

```kotlin
repositories { mavenLocal() }
implementation("io.github.manhtu227:json-to-view:0.1.0")
```

## Quick start

```kotlin
import com.manhtu.jsontoview.JsonToViewHost

val host = JsonToViewHost(context).apply {
    mode = JsonToViewHost.Mode.FLAT   // or NESTED
    bindJson(jsonString)             // or bind(fNode)
}
container.addView(host, MATCH_PARENT, MATCH_PARENT)
```

### Code tree (no JSON)

```kotlin
import com.manhtu.jsontoview.model.*

val root = FNode(
    kind = NodeKind.COLUMN,
    props = NodeProps(width = Dimension.MATCH, height = Dimension.MATCH, gap = 8),
    children = listOf(
        FNode(
            kind = NodeKind.TEXT,
            props = NodeProps(
                width = Dimension.WRAP,
                height = Dimension.WRAP,
                text = "Hello json-to-view",
                textSizeSp = 18f,
            ),
        ),
    ),
)
host.mode = JsonToViewHost.Mode.NESTED
host.bind(root)
```

### Parse only

```kotlin
import com.manhtu.jsontoview.JsonToView

val node = JsonToView.parse(json)
val spec = JsonToView.parseTree(json, name = "feed")
```

## Modules

| Path | Role |
|------|------|
| [`json-to-view/`](json-to-view/) | **SDK** (AAR) — model, parser, Flat, Nested, `JsonToViewHost`, optional benchmark runner |
| [`app/`](app/) | Sample — Launcher, Feed demo, Benchmark UI |

Package: `com.manhtu.jsontoview`

## Sample app

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

- **Feed Demo** — Flat host + `assets/view.json`
- **Benchmark** — Flat / Nested / Both, synthetic presets + feed, Logcat tag `FViewBench`

```bash
./gradlew :json-to-view:testDebugUnitTest :app:assembleDebug
./gradlew :json-to-view:publishToMavenLocal
```

## Architecture

```
JSON / FNode
     │
     ├─ FlatHostView   (canvas, performance)
     └─ NestedHost     (View hierarchy, debug)
     │
JsonToViewHost (mode switch)
```

Benchmark helpers (`BenchmarkRunner`, `BenchmarkReport`) ship in the library for tooling; the sample app UI is not part of the public product surface.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
