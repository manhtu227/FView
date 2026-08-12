# Feed template guide (backend + client)

## Goal

Backend emits card JSON; Android app renders with **json-to-view** and measures cost with **Benchmark**.

## Recommended card anatomy

```text
column (card)
 ├─ box (imageUrl)     // hero
 ├─ text (title)
 └─ text (subtitle)
 action on card root → navigate / open_url
```

See [schema/card.json](schema/card.json) and [schema/feed-page.json](schema/feed-page.json).

## Backend checklist

1. Prefer stable `"type"` field (not only legacy `viewType`).
2. Set `imageUrl` for remote images (https).
3. Set `action.type` + `action.payload` for taps.
4. Keep trees shallow when possible (depth 4–8 typical for cards).
5. Use `list` only when you need RecyclerView recycling; for short feeds a `column` of cards is fine.

## Client checklist

```kotlin
host.renderConfig = RenderConfig(
    imageLoader = GlideImageLoader(context), // app-owned
    actionHandler = { node, action -> /* navigate */ },
)
host.mode = JsonToViewHost.Mode.FLAT // feed hot path
host.bindJson(backendJson)
```

## Measuring cost

1. Save a production-like JSON as an asset or paste into samples.
2. Open sample app → **Benchmark**.
3. Sources include schema samples (hello/card/feed-page) and synthetic presets.
4. Run **Both** modes; compare `viewCount`, `firstFrameMs`, `measureMs`.
5. Logcat: `adb logcat -s FViewBench:I`

**Note:** Benchmark uses empty `RenderConfig` (no network images) so numbers stay deterministic; layout/hierarchy cost still reflects tree structure.
