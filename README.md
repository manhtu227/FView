# json-to-view

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Android%20API-24%2B-green.svg)](#)
[![CI](https://github.com/manhtu227/FView/actions/workflows/ci.yml/badge.svg)](https://github.com/manhtu227/FView/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/tag/manhtu227/FView?label=tag)](https://github.com/manhtu227/FView/tags)
[![JitPack](https://jitpack.io/v/manhtu227/FView.svg)](https://jitpack.io/#manhtu227/FView)

**json-to-view** is an Android SDK that renders a declarative UI tree (JSON or Kotlin) with **two backends**:

| Mode | Entry | Best for |
|------|--------|----------|
| **Flat** | `JsonToViewHost.Mode.FLAT` / `FlatHostView` | Performance — canvas draw, minimal Android `View` count |
| **Nested** | `JsonToViewHost.Mode.NESTED` / `NestedHost` | Debug & inspection — real `View` / `ViewGroup` tree |

Both consume the same `FNode` model so you can switch renderers without rewriting the tree.

> **0.1.1** — library module + sample app. Install via project module, `mavenLocal`, or [JitPack](https://jitpack.io).

## Why

Deep Android view hierarchies are expensive (measure/layout, memory, first frame). Server-driven UI and feed clients often map **one JSON node → one `View`**.

This library keeps **one tree** and offers:

- **Flat** — virtual layout + canvas (fewer framework views on hot paths)
- **Nested** — classic view tree (easier Layout Inspector / gradual adoption)
- **Benchmark tooling** — measure, layout, draw, first-frame, scroll, heap, view counts

Use it to ship a chosen backend, or to measure Flat vs Nested on your own trees.

## Features

- Shared model: `FNode`, `NodeKind`, `NodeProps`, `Dimension`, `TreeSpec`
- JSON parser for a practical `view.json`-style subset
- `JsonToViewHost` — single widget, switch `FLAT` / `NESTED`
- Optional `BenchmarkRunner` for A/B metrics
- Synthetic tree factory for depth/width sweeps
- Core path stays offline (no network image loads)

## Install

### JitPack (third-party apps)

**settings.gradle.kts**

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**app/build.gradle.kts**

```kotlin
dependencies {
    implementation("com.github.manhtu227.FView:json-to-view:v0.1.1")
}
```

> First JitPack build for a new tag can take a few minutes (Android SDK compile on their side).

### This monorepo

```kotlin
implementation(project(":json-to-view"))
```

### mavenLocal

```bash
./gradlew :json-to-view:publishToMavenLocal
```

```kotlin
repositories { mavenLocal() }
implementation("io.github.manhtu227:json-to-view:0.1.1")
```

## Quick start

```kotlin
import com.manhtu.jsontoview.JsonToViewHost
import android.view.ViewGroup

val host = JsonToViewHost(context).apply {
    mode = JsonToViewHost.Mode.FLAT   // or NESTED
    bindJson(jsonString)             // or bind(fNode)
}
container.addView(
    host,
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT,
)
```

### Build a tree in code

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

## JSON mapping

Legacy-style tree JSON (as in the sample `view.json`) maps as follows:

| Input | `NodeKind` |
|-------|------------|
| `viewType == 1` (list / RecyclerView) | `LIST` |
| `viewType == 2`, `orientation == 0` | `ROW` |
| `viewType == 2`, `orientation == 1` | `COLUMN` |
| `layoutType == 1` (stack) | `STACK` (wins over row/column) |
| `drawable.type == 1` (text), no meaningful children | `TEXT` |
| Image / button / icon drawable | `BOX` (solid color placeholder) |
| `viewType == 3` | `BOX` |

Dimensions: `width` / `height` with `value` + `unit` (`1` = dp, `2` = px, `3` = percent).  
Sentinels: `-1` = match parent, `-2` = wrap content.  
Unknown fields are ignored. Image loading is intentionally out of core scope.

## Public API

| Type | Role |
|------|------|
| `JsonToViewHost` | Main entry; `mode`, `bind`, `bindJson`, `clear` |
| `JsonToView` | `parse` / `parseTree` helpers |
| `FlatHostView` | Flat backend |
| `NestedHost` | Nested backend |
| `FNode`, `NodeProps`, … | Tree model |
| `JsonTreeParser` | JSON → `FNode` |
| `BenchmarkRunner` | Optional metrics (sample app uses this) |

Package: `com.manhtu.jsontoview`

## Architecture

```
JSON or Kotlin FNode
         │
      TreeSpec
    ┌────┴────┐
FlatHostView  NestedHost
 (canvas)     (Views)
    └────┬────┘
  JsonToViewHost
```

## Modules

| Path | Role |
|------|------|
| [`json-to-view/`](json-to-view/) | Publishable Android library (AAR) |
| [`app/`](app/) | Sample: launcher, feed demo, on-device benchmark UI |

## Sample app (demo)

See also [docs/demo.md](docs/demo.md).

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

| Screen | What it shows |
|--------|----------------|
| **Feed demo** | Parses `assets/view.json`, renders with **Flat** via `JsonToViewHost` |
| **Benchmark** | Flat / Nested / Both on feed or synthetic trees; Logcat tag `FViewBench` |

```bash
./gradlew :json-to-view:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :json-to-view:publishToMavenLocal
```

Sample benchmark notes (device-specific): [docs/benchmark-notes.md](docs/benchmark-notes.md).

## Status & roadmap

- [x] Dual backends + shared model  
- [x] JSON subset parser  
- [x] `JsonToViewHost`  
- [x] Sample feed + benchmark UI  
- [x] Unit tests + CI  
- [x] JitPack-oriented publish config  
- [ ] Maven Central  
- [ ] Pluggable image loader (app-supplied)  
- [ ] Dokka API site  
- [ ] Stable 1.0 API freeze  

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

```bash
./gradlew :json-to-view:testDebugUnitTest
```

Keep the core/benchmark path free of network image loading.

## Security

See [SECURITY.md](SECURITY.md).

## License

Apache License 2.0 — [LICENSE](LICENSE).

## Maintainers

- [manhtu227](https://github.com/manhtu227)
