# json-to-view

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Android%20API-24%2B-green.svg)](#)
[![CI](https://github.com/manhtu227/FView/actions/workflows/ci.yml/badge.svg)](https://github.com/manhtu227/FView/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/tag/manhtu227/FView?label=tag)](https://github.com/manhtu227/FView/tags)
[![JitPack](https://jitpack.io/v/manhtu227/FView.svg)](https://jitpack.io/#manhtu227/FView)

**json-to-view** is a small Android SDK for **server-driven UI (SDUI)**:

- **Backend / CMS** describes the screen as JSON (layout tree + content)
- **Your app** renders that JSON into real UI on the device

```text
Backend / CMS  ──JSON──►  json-to-view  ──►  Android UI
   (describes)              (builds)           (user sees)
```

The backend does **not** create Android `View`s. It only ships a **description**.  
This library is what **builds and draws** the UI on the client.

You can render the **same tree** with two engines:

| Mode | Class | Best for |
|------|--------|----------|
| **Flat** | `JsonToViewHost.Mode.FLAT` · `FlatHostView` | Performance — canvas host, few Android Views |
| **Nested** | `JsonToViewHost.Mode.NESTED` · `NestedHost` | Debug — real View hierarchy (Layout Inspector) |

> **Status:** `1.0.0` (early). Library + sample app. Install via monorepo module, `mavenLocal`, or [JitPack](https://jitpack.io/#manhtu227/FView).

---

## Who is this for?

| You… | How this helps |
|------|----------------|
| Ship **feed / campaign / config UI** from the server | Map JSON → screen without rewriting the app for every layout tweak |
| Care about **deep View hierarchies** | Try Flat on hot paths; keep Nested for debug or simple screens |
| Need a **fair Flat vs Nested comparison** | Same `FNode` tree, optional benchmark metrics |
| Learn Android layout cost | Sample app shows feed render + on-device numbers |

**Not** a full design system, not a Compose replacement, not a consumer social app.

---

## Why it exists

Typical SDUI stacks do **1 JSON node → 1 Android View**. Deep trees get expensive (measure/layout, first frame, memory, scroll).

**json-to-view** keeps:

1. **One model** — `FNode` / `TreeSpec` (from JSON or Kotlin)
2. **Two renderers** — Flat (canvas) and Nested (Views)
3. **Optional benchmarks** — measure / layout / draw / first-frame / scroll / heap / viewCount

So teams can **describe UI on the backend**, **render on the client**, and **measure** before locking an architecture.

---

## Features

- Shared pure model: `FNode`, `NodeKind`, `NodeProps`, `Dimension`, `TreeSpec`
- JSON parser for a practical legacy-style `view.json` subset
- `JsonToViewHost` — one widget, switch `FLAT` / `NESTED`
- `LIST` → `RecyclerView` (per-item host for fair list comparison)
- Optional `BenchmarkRunner` + sample Benchmark UI
- Synthetic trees (shallow / deep / wide / feed-like) for sweeps
- Core path stays **offline** (no network image loader in the library hot path)

---

## Install

### JitPack (recommended for other apps)

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
    implementation("com.github.manhtu227.FView:json-to-view:v1.0.0")
}
```

### This repository

```kotlin
implementation(project(":json-to-view"))
```

### mavenLocal

```bash
./gradlew :json-to-view:publishToMavenLocal
```

```kotlin
repositories { mavenLocal() }
implementation("io.github.manhtu227:json-to-view:1.0.0")
```

---

## Quick start

```kotlin
import com.manhtu.jsontoview.JsonToViewHost
import android.view.ViewGroup

// JSON from your backend / assets / CMS
val host = JsonToViewHost(context).apply {
    mode = JsonToViewHost.Mode.FLAT   // or NESTED
    bindJson(jsonString)
}

container.addView(
    host,
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT,
)
```

### Build a tree in code (no JSON)

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

---

## JSON mapping

How backend-style JSON becomes `NodeKind` (subset used by the sample `view.json`):

| Backend JSON | Client `NodeKind` |
|--------------|-------------------|
| `viewType == 1` (list) | `LIST` |
| `viewType == 2`, `orientation == 0` | `ROW` |
| `viewType == 2`, `orientation == 1` | `COLUMN` |
| `layoutType == 1` (stack) | `STACK` (overrides row/column) |
| Text drawable, no meaningful children | `TEXT` |
| Image / button / icon drawable | `BOX` (solid color in core; no Glide) |
| `viewType == 3` | `BOX` |

**Size:** `width` / `height` with `value` + `unit` (`1` = dp, `2` = px, `3` = percent).  
**Sentinels:** `-1` = match parent, `-2` = wrap content.  
Unknown fields are ignored. Apps own image loading if they add it later.

---

## Public API

| API | Role |
|-----|------|
| `JsonToViewHost` | Main entry: `mode`, `bind`, `bindJson`, `clear` |
| `JsonToView` | `parse` / `parseTree` helpers |
| `FlatHostView` | Flat (canvas) backend |
| `NestedHost` | Nested (View tree) backend |
| `FNode`, `NodeProps`, … | Tree model |
| `JsonTreeParser` | JSON → `FNode` |
| `BenchmarkRunner` | Optional metrics tooling |

Package: `com.manhtu.jsontoview`

---

## Architecture

```
Backend JSON or Kotlin FNode
            │
         TreeSpec / FNode      ← one shared model
       ┌────┴────┐
 FlatHostView   NestedHost
  (canvas)       (Views)
       └────┬────┘
     JsonToViewHost            ← mode switch
            │
     BenchmarkRunner           ← optional
```

---

## Modules in this repo

| Path | Role |
|------|------|
| [`json-to-view/`](json-to-view/) | **SDK** (publishable AAR) — what other apps depend on |
| [`app/`](app/) | **Sample only** — not published; proves the SDK works |

### Sample app

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

| Screen | Purpose |
|--------|---------|
| **Feed demo** | Load `assets/view.json`, render with **Flat** (typical production path) |
| **Benchmark** | Flat / Nested / Both · synthetic or feed · Logcat `FViewBench` |

More detail: [docs/demo.md](docs/demo.md) · sample numbers: [docs/benchmark-notes.md](docs/benchmark-notes.md)

```bash
./gradlew :json-to-view:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :json-to-view:publishToMavenLocal
```

---

## Schema & SDUI

- **Schema:** [docs/schema.md](docs/schema.md) — `type`, `imageUrl`, `action`
- **Samples:** [docs/schema/](docs/schema/) (`hello`, `card`, `feed-page`)
- **Feed templates:** [docs/feed-templates.md](docs/feed-templates.md)
- **Stable mapping 1.0:** [docs/MAPPING_STABLE.md](docs/MAPPING_STABLE.md)
- **API freeze:** [docs/api-1.0.md](docs/api-1.0.md)
- **Consumer demo:** module `:consumer-demo` (second app using the library)

### Image + action (app code)

```kotlin
host.renderConfig = RenderConfig(
    imageLoader = MyGlideLoader(context), // you provide
    actionHandler = { node, action -> /* navigate */ },
)
host.bindJson(backendJson)
```

Core stays free of Glide; inject loaders only in the app.

## Status & roadmap

- [x] Dual backends + shared model  
- [x] JSON subset parser  
- [x] `JsonToViewHost` public entry  
- [x] Sample feed + benchmark UI  
- [x] Unit tests + CI  
- [x] JitPack-oriented packaging (`v1.0.0`)  
- [ ] Maven Central  
- [ ] App-provided image loader hook  
- [ ] Stable JSON schema docs for 1.0  
- [ ] Dokka API site  

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

```bash
./gradlew :json-to-view:testDebugUnitTest
```

Please keep the core / benchmark path free of network image loading.

## Security

See [SECURITY.md](SECURITY.md).

## License

Apache License 2.0 — [LICENSE](LICENSE).

## Maintainers

- [manhtu227](https://github.com/manhtu227)
