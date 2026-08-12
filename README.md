# json-to-view

<p align="center">
  <strong>Drop a JSON tree on the client — the app builds the screen the user sees, natively.</strong><br/>
  One model. Two renderers. Measure cost. Optional AI studio in the sample app only.
</p>

<p align="center">
  <em>same&nbsp;FNode · Flat&nbsp;(canvas)&nbsp;/&nbsp;Nested&nbsp;(Views) · offline&nbsp;core · app-owned&nbsp;images&nbsp;&amp;&nbsp;actions</em>
</p>

<p align="center">
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/LICENSE-Apache%202.0-7c83ff?style=for-the-badge&labelColor=3d3d4a" /></a>
  <img alt="Kotlin" src="https://img.shields.io/badge/KOTLIN-Android-7F52FF?style=for-the-badge&labelColor=3d3d4a&logo=kotlin&logoColor=white" />
  <img alt="API" src="https://img.shields.io/badge/MIN_SDK-24%2B-2ea5e9?style=for-the-badge&labelColor=3d3d4a" />
  <img alt="SDUI" src="https://img.shields.io/badge/SDUI-JSON%20→%20UI-0ea5e9?style=for-the-badge&labelColor=3d3d4a" />
  <img alt="Render" src="https://img.shields.io/badge/RENDER-Flat%20%7C%20Nested-111827?style=for-the-badge&labelColor=3d3d4a" />
  <img alt="Core" src="https://img.shields.io/badge/CORE-Offline-14b8a6?style=for-the-badge&labelColor=3d3d4a" />
  <img alt="AI" src="https://img.shields.io/badge/AI-Sample%20only-f472b6?style=for-the-badge&labelColor=3d3d4a" />
</p>

<p align="center">
  <a href="https://github.com/manhtu227/FView/actions/workflows/ci.yml"><img alt="CI" src="https://img.shields.io/github/actions/workflow/status/manhtu227/FView/ci.yml?branch=main&style=for-the-badge&label=CI&labelColor=3d3d4a" /></a>
  <a href="https://github.com/manhtu227/FView/releases"><img alt="Version" src="https://img.shields.io/badge/v1.0.0-API%20freeze-22c55e?style=for-the-badge&labelColor=3d3d4a" /></a>
  <img alt="Status" src="https://img.shields.io/badge/STATUS-OPEN%20SOURCE-f59e0b?style=for-the-badge&labelColor=3d3d4a" />
</p>

<p align="center">
  <a href="#the-pointer">The pointer</a> ·
  <a href="#-how-it-works">How it works</a> ·
  <a href="#-packages">Packages</a> ·
  <a href="#️-configuration">Configuration</a> ·
  <a href="#-the-gate">The gate</a> ·
  <a href="#-seen">Seen</a> ·
  <a href="#-how-it-compares">Compare</a> ·
  <a href="#-faq">FAQ</a>
</p>

> **Note**  
> Public SDK API is frozen for **v1.x** — see [docs/api-1.0.md](docs/api-1.0.md).  
> AI Layout Studio lives in the **sample app only** (not the published AAR). Feedback welcome.

---

## The pointer

In this project the **pointer** is the **declarative UI tree** the backend (or AI, or Kotlin code) hands the client:

| Layer | What it is |
|-------|------------|
| **JSON** | What backend / CMS / AI emits (`type`, `props`, `children`) |
| **`FNode`** | In-memory tree after parse |
| **`JsonToViewHost`** | Single widget that “hosts” that tree and draws it |

You do **not** drop Android Views from the server. You drop a **description**. The host moves in and builds what the user sees — **natively** (Flat canvas or Nested Views).

```text
Backend / CMS / AI
        │  JSON tree  ← the pointer
        ▼
  JsonToViewHost.bindJson(...)
        │
   ┌────┴────┐
 Flat     Nested     ← same tree, two ways to draw
        │
   User screen
```

Stable mapping: [docs/MAPPING_STABLE.md](docs/MAPPING_STABLE.md) · full schema: [docs/schema.md](docs/schema.md)

---

## 🧭 How it works

1. **Describe** — backend (or sample AI Studio) produces JSON or you build `FNode` in Kotlin.  
2. **Parse** — `JsonTreeParser` / `JsonToView.parse` → `FNode`.  
3. **Configure** — optional `RenderConfig` (image loader + action handler from **your app**).  
4. **Render** — `JsonToViewHost` with `Mode.FLAT` or `Mode.NESTED`.  
5. **Measure** (optional) — `BenchmarkRunner` compares cost on the **same** tree.

```kotlin
val host = JsonToViewHost(context).apply {
    mode = JsonToViewHost.Mode.FLAT
    renderConfig = RenderConfig(
        imageLoader = MyImageLoader(context),      // app provides
        actionHandler = { node, action -> /* navigate */ },
    )
    bindJson(backendJson)
}
container.addView(host, MATCH_PARENT, MATCH_PARENT)
```

| Mode | Best for |
|------|----------|
| **FLAT** | Performance — few Android `View`s, canvas draw |
| **NESTED** | Debug / inspection — real `View` hierarchy |

---

## 📦 Packages

| Package / module | Role |
|------------------|------|
| **`com.manhtu.jsontoview`** | `JsonToViewHost`, `JsonToView`, `RenderConfig` |
| **`.model`** | `FNode`, `NodeProps`, `NodeAction`, `Dimension`, … |
| **`.parse`** | `JsonTreeParser` |
| **`.flat`** | `FlatHostView` |
| **`.nested`** | `NestedHost`, `NestedTreeBuilder` |
| **`.image` / `.action`** | `ImageLoader`, `NodeActionHandler` (contracts only) |
| **`.benchmark`** | `BenchmarkRunner`, reports (tooling) |
| **`:json-to-view`** | Publishable AAR |
| **`:app`** | Sample (feed, samples, AI Studio, benchmark) — not published |
| **`:consumer-demo`** | Second app using the library as a consumer |

Install (JitPack):

```kotlin
// settings.gradle.kts
maven { url = uri("https://jitpack.io") }

// app/build.gradle.kts
implementation("com.github.manhtu227.FView:json-to-view:v1.0.0")
```

Monorepo: `implementation(project(":json-to-view"))`  
Local: `./gradlew :json-to-view:publishToMavenLocal` → `io.github.manhtu227:json-to-view:1.0.0`

---

## ⚙️ Configuration

### Host

| Setting | Meaning |
|---------|---------|
| `mode` | `FLAT` or `NESTED` |
| `bind` / `bindJson` / `clear` | Attach or detach the tree |
| `renderConfig` | Images + taps (see below) |

### `RenderConfig` (app-owned)

```kotlin
RenderConfig(
    imageLoader = …,              // null → solid placeholder (bench-safe)
    actionHandler = …,            // null → taps ignored
    imagePlaceholderColor = 0xFF888888.toInt(),
)
```

| Concern | Where it lives |
|---------|----------------|
| Network images (Glide/Coil) | **Your app** implements `ImageLoader` |
| Navigation / analytics on tap | **Your app** implements `NodeActionHandler` |
| Layout measure/draw | **SDK** |
| AI API keys | **Sample / your server only** — never in the AAR |

### Sample AI Studio (optional)

| Key | Where |
|-----|--------|
| `AI_API_KEY` | `local.properties` (debug only, gitignored) |
| `AI_BASE_URL` / `AI_MODEL` | BuildConfig defaults (OpenAI-compatible) |

See [docs/ai-studio.md](docs/ai-studio.md). CLI: `python3 scripts/ai_layout.py "…" -o out.json`

---

## 🚦 The gate

Nothing untrusted should reach the screen without checks:

| Gate | What happens |
|------|----------------|
| **Parse** | `JsonTreeParser.parse` — invalid JSON / shape → fail before bind |
| **Schema** | Stable `type` + props ([MAPPING_STABLE](docs/MAPPING_STABLE.md)); unknown fields ignored |
| **Images** | No loader in core → placeholder only; your loader decides network |
| **Actions** | No handler → no side effects; your handler decides navigate/open |
| **AI (sample)** | Model output must parse; bad JSON is not bound |
| **Benchmark** | Default `RenderConfig()` — no network, deterministic cost |

Public surface and freeze rules: [docs/api-1.0.md](docs/api-1.0.md).

---

## 👁 Seen

What the **user** actually sees is always **native Android UI**, not a webview:

| Source | What appears on device |
|--------|-------------------------|
| `text` | Text (Flat canvas / Nested `TextView`) |
| `box` + color | Colored rect |
| `box` + `imageUrl` | Image via **your** loader, or gray placeholder |
| `row` / `column` / `stack` | Layout structure |
| `list` | Vertical `RecyclerView` of item subtrees |
| `action` | Tap → your handler (toast/navigate/…) |

**Same JSON** → switch Flat ↔ Nested → same content, different hierarchy cost.

**See it running:**

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

| Screen | What you see |
|--------|----------------|
| Feed / samples | Real JSON trees on screen |
| AI Layout Studio | Prompt → generated UI |
| Benchmark | Numbers: measure, layout, first frame, viewCount, … |

Samples: [docs/schema/](docs/schema/) · demo notes: [docs/demo.md](docs/demo.md)

---

## 🆚 How it compares

| Approach | vs **json-to-view** |
|----------|---------------------|
| **1 JSON node → 1 View** (classic SDUI) | Nested mode is similar; **Flat** can cut view count on deep trees |
| **Hardcoded XML / Compose only** | Faster for static apps; you **lose** remote layout without shipping app |
| **WebView for remote UI** | Heavier, different a11y/perf model; this stays **native** |
| **Full design system / CMS SDK** | We stay **small**: model + two renderers + hooks — not themes/components catalog |
| **AI-in-the-library** | AI is **sample-only**; production AI should sit on **your server**, app only binds JSON |

**Fair comparison built-in:** same `TreeSpec` / `FNode` → Flat and Nested → `BenchmarkRunner` (Logcat `FViewBench`).

---

## ❓ FAQ

**Is the backend building Android Views?**  
No. Backend (or AI) builds a **description**. The app builds Views/canvas.

**Do I need AI?**  
No. AI Studio is optional sample tooling. Production path is JSON from your backend.

**Where does the API key go?**  
Not in the published SDK. Sample: `AI_API_KEY` in gitignored `local.properties` (debug only). Production: server proxy.

**Flat or Nested?**  
Flat for hot paths / deep trees; Nested when you want Layout Inspector and classic hierarchy. You can switch without changing JSON.

**Images / Glide in the library?**  
No. Implement `ImageLoader` in the app (sample uses Glide).

**Can I use only Kotlin, no JSON?**  
Yes — build `FNode` trees and `host.bind(root)`.

**Is the API stable?**  
v1.x public API is documented and frozen for breaking changes — [docs/api-1.0.md](docs/api-1.0.md).

**How do I measure cost?**  
Sample **Benchmark** screen, or `BenchmarkRunner` in your app. Prefer empty `RenderConfig` for deterministic runs.

**Maven Central?**  
JitPack / `mavenLocal` today; Central steps in [docs/publishing.md](docs/publishing.md).

---

## Quick start (copy-paste)

```kotlin
implementation("com.github.manhtu227.FView:json-to-view:v1.0.0")
// + maven { url = uri("https://jitpack.io") }
```

```kotlin
import com.manhtu.jsontoview.JsonToViewHost

val host = JsonToViewHost(context).apply {
    mode = JsonToViewHost.Mode.FLAT
    bindJson(jsonFromBackend)
}
```

More: [docs/schema.md](docs/schema.md) · [docs/feed-templates.md](docs/feed-templates.md) · [CHANGELOG.md](CHANGELOG.md)

---

## Contributing & license

```bash
./gradlew :json-to-view:testDebugUnitTest :app:assembleDebug
```

- [CONTRIBUTING.md](CONTRIBUTING.md) · [SECURITY.md](SECURITY.md)  
- Apache License 2.0 — [LICENSE](LICENSE)  
- Maintainer: [manhtu227](https://github.com/manhtu227)
