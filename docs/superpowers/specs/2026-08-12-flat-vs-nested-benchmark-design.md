# Design: FlatView vs Nested Views Benchmark

**Date:** 2026-08-12  
**Project:** FView / JsonToView  
**Status:** Approved for implementation planning  

## 1. Goal

Rewrite the FView engine into a clean dual-renderer architecture and add a full benchmark harness to compare:

1. **Flat canvas rendering** (single host `ViewGroup`, virtual tree drawn on Canvas)  
2. **Nested Android Views** (each tree node becomes a real `View` / `ViewGroup`)  

Also sweep **tree depth / node count** (synthetic trees) and support the **real `view.json` feed**.

**Outcome:** measurable answer to *where nested hierarchies win or lose* vs flat rendering (time + memory + scroll).

## 2. Non-goals (YAGNI)

- Full visual parity with every existing prop (complex borders, Glide images in benchmark path, comment UI).
- Network-dependent image loads during benchmark (use solid color rects).
- Multi-threaded measure/layout.
- Keeping dead code (`FViewComment` WIP, debug-only paths, sparse `MainActivity2` demo as primary UX).

## 3. Decisions (locked)

| Topic | Choice |
|-------|--------|
| Comparison | **C:** Flat vs Nested **and** depth/node sweeps |
| Metrics | **Full:** measure/layout/draw/first-frame + scroll jank + memory |
| Content | Real `view.json` feed **+** synthetic tree factory |
| Rewrite depth | Clean rewrite of FView structure + new Nested renderer + Benchmark harness |

## 4. Architecture

```
TreeSpec / FNode   (pure model, no Android View dependency)
        │
        ├──────────────► FlatRenderer  → FlatHostView
        │                  (1 host, canvas recursive draw)
        │
        └──────────────► NestedRenderer → NestedHost
                           (real View hierarchy)
        │
BenchmarkRunner
  · cold/warm measure + layout + draw
  · scroll fling → frame times / drops
  · memory: view count, max depth, Java heap, PSS
  · median of N runs → BenchmarkReport → UI + Logcat
```

### 4.1 Package layout

```
com.demo.jsontoview
  model/       FNode, NodeKind, NodeProps, Dimension, TreeSpec, SyntheticTreeFactory
  parse/       JsonTreeParser (Gson → FNode)
  flat/        FlatHostView, FlatLayout, FlatDraw, leaf text/box
  nested/      NestedTreeBuilder, NestedHost
  benchmark/   BenchmarkRunner, MetricsCollector, BenchmarkReport,
               BenchmarkActivity, presets UI
  demo/        FeedActivity (flat render of view.json)
  util/        Dimens, Timing, HierarchyStats
```

Legacy files (`FView.kt`, `CustomViewGroup2.kt`, scattered top-level enums in `data.kt`, etc.) are replaced or thin-wrapped; dead code removed.

### 4.2 Shared model

```kotlin
enum class NodeKind { ROW, COLUMN, STACK, BOX, TEXT, LIST }

data class Dimension(val value: Int, val unit: Unit) // MATCH=-1, WRAP=-2, px/dp/%

data class NodeProps(
  val id: String? = null,
  val width: Dimension,
  val height: Dimension,
  val padding: EdgeInsets = EdgeInsets.ZERO,
  val margin: EdgeInsets = EdgeInsets.ZERO,
  val gap: Int = 0,
  val backgroundColor: Int? = null,
  val cornerRadius: Float = 0f,
  val text: String? = null,
  val textSizeSp: Float = 14f,
  val textColor: Int = 0xFF000000.toInt(),
)

data class FNode(
  val kind: NodeKind,
  val props: NodeProps,
  val children: List<FNode> = emptyList(),
)

data class TreeSpec(
  val name: String,
  val root: FNode,
  val source: Source, // FEED_JSON | SYNTHETIC
)
```

**Mapping from existing `view.json`:**

| JSON | FNode |
|------|--------|
| ViewGroup + orientation H | ROW |
| ViewGroup + orientation V | COLUMN |
| layoutType Stack | STACK |
| drawable Text | TEXT |
| drawable Image / Button / Icon | BOX (solid color placeholder in bench; optional later) |
| viewType RecyclerView | LIST |

Parser may ignore unknown fields. Feed demo may later re-enable richer leaf types; **benchmark path stays deterministic** (no Glide).

### 4.3 Synthetic trees

`SyntheticTreeFactory`:

| Preset | Intent |
|--------|--------|
| `shallow` | depth=3, ~50 nodes |
| `deep` | depth=20, branching low, ~40 nodes |
| `wide` | depth=2, ~200 nodes |
| `feedLike` | depth~6–7, list of N cards |
| Custom | user-set depth D, branching B |

Same `TreeSpec` is fed to both renderers.

## 5. Renderers

### 5.1 Flat (`FlatHostView`)

- Single `ViewGroup` (or `View`) host.
- Holds `FNode` root; recursive measure → layout → draw on `Canvas`.
- ROW/COLUMN: linear packing with gap.
- STACK: children share origin (offset by margin).
- TEXT: `StaticLayout` / `TextPaint`.
- BOX: `drawRect` / `drawRoundRect`.
- LIST: host embeds one `RecyclerView`; each item is a nested `FlatHostView` for the item subtree **or** flat-draws item in one host per row (prefer **one FlatHostView per list item** for fair scroll comparison).

Touch is optional for benchmark (no-op or simple hit-test); not a success metric.

### 5.2 Nested (`NestedTreeBuilder`)

- ROW/COLUMN → `LinearLayout` HORIZONTAL/VERTICAL + gap via margins or spacer views (margin on children preferred).
- STACK → `FrameLayout`.
- TEXT → `TextView`.
- BOX → `View` / `FrameLayout` with background.
- LIST → `RecyclerView` + adapter inflating nested subtree per item.
- Every node allocates ≥1 Android `View` so hierarchy depth ≈ tree depth.

### 5.3 Fairness rules

- Same `TreeSpec`, same text content, same colors/sizes.
- No images / network in benchmark runs.
- Same parent container size (match_parent).
- Detach previous tree fully before each run to avoid view leaks skewing memory.

## 6. Metrics (full)

| Metric | Method |
|--------|--------|
| `measureMs` | `System.nanoTime()` around `measure` |
| `layoutMs` | around `layout` |
| `drawMs` | first draw after layout (`OnDrawListener` / pre-draw + post-frame) |
| `firstFrameMs` | bind start → first drawn frame |
| `scrollAvgFrameMs` | `Choreographer.FrameCallback` during ~1s fling |
| `droppedFrames` | frames with Δt > 18ms (or 2× vs 16.6ms) while scrolling |
| `viewCount` | walk Android hierarchy from host |
| `treeNodeCount` | walk `FNode` |
| `maxViewDepth` | max depth of Android hierarchy |
| `maxTreeDepth` | max depth of `FNode` |
| `javaHeapMb` | `(totalMemory - freeMemory) / 1MB` after GC optional toggle |
| `pssMb` | `Debug.getPss()` when available |

**Run protocol:**

1. Optional `Runtime.gc()` (flag; default off for primary numbers, on for memory section).  
2. **3 warm-up** renders (discard).  
3. **5 measured** runs → **median** (+ min/max logged).  
4. Scroll phase once per configuration after warm layout (or median of 3 flings if time allows).

`BenchmarkReport` is a data class serializable to log lines and shown in UI table. Include delta:

```
deltaPct(nested, flat) = (nested - flat) / flat * 100
```

## 7. UI

### 7.1 Launcher

- **Feed Demo** → `FeedActivity` (flat render `assets/view.json`, product-like).  
- **Benchmark** → `BenchmarkActivity`.

### 7.2 BenchmarkActivity

Controls:

- Mode: Flat | Nested | Both (sequential: Flat then Nested)  
- Source: Feed JSON | Synthetic presets | Custom D/B  
- Buttons: Run, Clear  
- Results: table (metric × renderer), delta column, copy/log export via Logcat tag `FViewBench`

Display host: full-width container; for scroll tests use vertical `RecyclerView`/`ScrollView` with enough content to fling.

## 8. Implementation phases

1. **Model + parser + synthetic factory** (unit-testable pure Kotlin where possible).  
2. **FlatHostView** minimal ROW/COLUMN/TEXT/BOX/LIST.  
3. **NestedTreeBuilder** parity for same kinds.  
4. **BenchmarkRunner + MetricsCollector**.  
5. **BenchmarkActivity UI** + launcher.  
6. **FeedActivity** wired to flat path + `view.json`.  
7. **Delete/stop shipping** dead legacy entry points; keep git history.  
8. **Manual verification** on emulator: all presets run, numbers stable order-of-magnitude.

## 9. Success criteria

- [ ] Same `TreeSpec` renders in both modes without crash.  
- [ ] Full metric set collected for Flat and Nested.  
- [ ] Synthetic deep/wide/shallow presets runnable.  
- [ ] Feed JSON loads in Feed Demo (flat).  
- [ ] Results show clear deltas (e.g. Nested viewCount ≫ Flat; deep Nested measure/layout worse).  
- [ ] No Glide/network in benchmark path.  
- [ ] Code organized under packages in §4.1; no reliance on commented-out WIP UI.

## 10. Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Feed JSON has richer props than FNode | Parser maps subset; placeholders for image/button |
| Timing noise on emulator | Warm-up + median; log min/max; document device |
| Nested gap/padding mismatch vs Flat | Shared `NodeProps` interpretation helpers |
| Memory noise | Optional GC; report both heap and viewCount (stable) |
| LIST double-count complexity | Document: list items each host their subtree; same for both modes |

## 11. Out-of-scope follow-ups (later)

- Re-enable ImageDrawable/Glide for Feed Demo only.  
- Click actions / like button parity.  
- Export CSV of benchmark reports.  
- Automated instrumentation tests for timing thresholds (flaky by nature).

## 12. Approval

- Design discussion approved by user: **OK** (2026-08-12).  
- Next step after user confirms this written spec: implementation plan (`writing-plans`), then code.
