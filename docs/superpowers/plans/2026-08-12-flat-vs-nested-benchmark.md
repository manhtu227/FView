# FlatView vs Nested Views Benchmark — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clean dual-renderer architecture (Flat canvas + Nested Android Views) with a full benchmark harness (measure/layout/draw/first-frame, scroll jank, memory) over real `view.json` and synthetic trees.

**Architecture:** Pure `FNode`/`TreeSpec` model feeds two independent renderers (`FlatHostView`, `NestedTreeBuilder`). `BenchmarkRunner` times layout/draw/scroll and collects hierarchy/heap stats into `BenchmarkReport`. UI: `FeedActivity` (demo) + `BenchmarkActivity` (controls + results table).

**Tech Stack:** Kotlin, Android SDK 24–35, AppCompat, RecyclerView, Gson, JUnit4 (JVM unit tests for model/parser/synthetic). No Glide on benchmark path.

**Spec:** `docs/superpowers/specs/2026-08-12-flat-vs-nested-benchmark-design.md`

## Global Constraints

- Benchmark path: **no network / no Glide**; images map to solid color BOX.
- Same `TreeSpec` for Flat and Nested; detach old tree before each run.
- Timing protocol: **3 warm-up + 5 measured → median** (min/max logged).
- Package root: `com.demo.jsontoview` with subpackages `model`, `parse`, `flat`, `nested`, `benchmark`, `demo`, `util`.
- JVM target 17; do not bump AGP/Gradle in this work unless build is broken.
- YAGNI: no comment UI, no click animations, no CSV export.

## File map (create / replace)

| Path | Responsibility |
|------|----------------|
| `app/src/main/java/com/demo/jsontoview/model/*.kt` | FNode, enums, props, TreeSpec, SyntheticTreeFactory |
| `app/src/main/java/com/demo/jsontoview/parse/JsonTreeParser.kt` | Gson map view.json → FNode |
| `app/src/main/java/com/demo/jsontoview/util/*.kt` | Dimens, HierarchyStats, Timing |
| `app/src/main/java/com/demo/jsontoview/flat/FlatHostView.kt` | Flat measure/layout/draw host |
| `app/src/main/java/com/demo/jsontoview/nested/NestedTreeBuilder.kt` | Build real View tree |
| `app/src/main/java/com/demo/jsontoview/nested/NestedHost.kt` | Container that holds built tree |
| `app/src/main/java/com/demo/jsontoview/benchmark/*.kt` | Runner, report, activity, adapter |
| `app/src/main/java/com/demo/jsontoview/demo/FeedActivity.kt` | Flat feed from assets |
| `app/src/main/java/com/demo/jsontoview/demo/LauncherActivity.kt` | Choose Feed / Benchmark |
| `app/src/main/res/layout/activity_benchmark.xml` | Benchmark UI |
| `app/src/main/res/layout/activity_launcher.xml` | Launcher UI |
| `app/src/main/res/layout/activity_feed.xml` | Feed host container |
| `app/src/main/AndroidManifest.xml` | Activities registration |
| `app/src/test/java/com/demo/jsontoview/...` | JVM unit tests |
| Legacy under old paths | Delete after new path works (Task 8) |

---

### Task 1: Model + Dimens + unit tests

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/model/NodeKind.kt`
- Create: `app/src/main/java/com/demo/jsontoview/model/Dimension.kt`
- Create: `app/src/main/java/com/demo/jsontoview/model/NodeProps.kt`
- Create: `app/src/main/java/com/demo/jsontoview/model/FNode.kt`
- Create: `app/src/main/java/com/demo/jsontoview/model/TreeSpec.kt`
- Create: `app/src/main/java/com/demo/jsontoview/util/Dimens.kt`
- Create: `app/src/test/java/com/demo/jsontoview/model/FNodeStatsTest.kt`

**Interfaces:**
- Produces:
  - `enum class NodeKind { ROW, COLUMN, STACK, BOX, TEXT, LIST }`
  - `enum class DimUnit { DP, PX, PERCENT }`
  - `data class Dimension(val value: Int, val unit: DimUnit = DimUnit.DP)` with constants `MATCH = Dimension(-1)`, `WRAP = Dimension(-2)`
  - `data class EdgeInsets(val left: Int = 0, val top: Int = 0, val right: Int = 0, val bottom: Int = 0)` companion `ZERO`
  - `data class NodeProps(...)` as in spec §4.2
  - `data class FNode(val kind: NodeKind, val props: NodeProps, val children: List<FNode> = emptyList())`
  - `fun FNode.nodeCount(): Int` and `fun FNode.maxDepth(): Int`
  - `enum class TreeSource { FEED_JSON, SYNTHETIC }`
  - `data class TreeSpec(val name: String, val root: FNode, val source: TreeSource)`
  - `object Dimens { fun dp(context, v: Int): Int; fun resolve(dim: Dimension, parentSize: Int, density: Float): Int }`

- [ ] **Step 1: Write failing unit test for tree stats**

```kotlin
package com.demo.jsontoview.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FNodeStatsTest {
    @Test
    fun nodeCount_and_maxDepth() {
        val leaf = FNode(NodeKind.TEXT, NodeProps(width = Dimension.WRAP, height = Dimension.WRAP, text = "a"))
        val row = FNode(NodeKind.ROW, NodeProps(width = Dimension.MATCH, height = Dimension.WRAP), listOf(leaf, leaf))
        val root = FNode(NodeKind.COLUMN, NodeProps(width = Dimension.MATCH, height = Dimension.MATCH), listOf(row))
        assertEquals(4, root.nodeCount())
        assertEquals(3, root.maxDepth())
    }
}
```

- [ ] **Step 2: Run test — expect compile/fail**

Run: `./gradlew :app:testDebugUnitTest --tests com.demo.jsontoview.model.FNodeStatsTest`  
Expected: FAIL (types missing)

- [ ] **Step 3: Implement model + Dimens + extension stats**

Implement files listed above. `nodeCount` = 1 + sum children; `maxDepth` = 1 + max child depth (leaf = 1).  
`Dimens.resolve`: `-1` → `parentSize`, `-2` → treated as wrap sentinel return `ViewGroup.LayoutParams.WRAP_CONTENT` only at call sites that need LayoutParams — for pure resolve of fixed sizes: `value * density` for DP, `value` for PX, `parentSize * value / 100` for PERCENT.

- [ ] **Step 4: Run test — expect PASS**

Run: `./gradlew :app:testDebugUnitTest --tests com.demo.jsontoview.model.FNodeStatsTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/demo/jsontoview/model app/src/main/java/com/demo/jsontoview/util/Dimens.kt app/src/test/java/com/demo/jsontoview/model
git commit -m "feat(model): add FNode tree model and dimens helpers"
```

---

### Task 2: SyntheticTreeFactory

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/model/SyntheticTreeFactory.kt`
- Create: `app/src/test/java/com/demo/jsontoview/model/SyntheticTreeFactoryTest.kt`

**Interfaces:**
- Consumes: `FNode`, `NodeKind`, `NodeProps`, `Dimension`, `TreeSpec`, `TreeSource`
- Produces:
  - `object SyntheticTreeFactory`
  - `fun shallow(): TreeSpec` — depth 3, ~50 nodes
  - `fun deep(): TreeSpec` — depth 20, low branch, ~40 nodes
  - `fun wide(): TreeSpec` — depth 2, ~200 nodes
  - `fun feedLike(cards: Int = 15): TreeSpec` — COLUMN list of card-like COLUMN subtrees depth ~6
  - `fun custom(depth: Int, branching: Int): TreeSpec`

- [ ] **Step 1: Write failing tests**

```kotlin
package com.demo.jsontoview.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntheticTreeFactoryTest {
    @Test
    fun deep_hasExpectedDepth() {
        val t = SyntheticTreeFactory.deep()
        assertEquals(20, t.root.maxDepth())
        assertTrue(t.root.nodeCount() in 20..80)
        assertEquals(TreeSource.SYNTHETIC, t.source)
    }

    @Test
    fun wide_isShallowButManyNodes() {
        val t = SyntheticTreeFactory.wide()
        assertTrue(t.root.maxDepth() <= 3)
        assertTrue(t.root.nodeCount() >= 180)
    }
}
```

- [ ] **Step 2: Run tests — expect FAIL**

Run: `./gradlew :app:testDebugUnitTest --tests com.demo.jsontoview.model.SyntheticTreeFactoryTest`  
Expected: FAIL

- [ ] **Step 3: Implement factory**

Build recursively: at depth remaining 1 emit TEXT leaf; else emit COLUMN/ROW alternating with `branching` children. Colors cycle a small palette (ARGB ints). Names: `"synthetic-deep"`, etc.

- [ ] **Step 4: Run tests — PASS**

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/demo/jsontoview/model/SyntheticTreeFactory.kt app/src/test/java/com/demo/jsontoview/model/SyntheticTreeFactoryTest.kt
git commit -m "feat(model): synthetic tree presets for benchmark"
```

---

### Task 3: JsonTreeParser (view.json → FNode)

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/parse/JsonTreeParser.kt`
- Create: `app/src/test/java/com/demo/jsontoview/parse/JsonTreeParserTest.kt`
- Create: `app/src/test/resources/sample_tree.json` (tiny fixture, not full 15k feed)

**Interfaces:**
- Consumes: Gson, model types
- Produces:
  - `object JsonTreeParser`
  - `fun parse(json: String): FNode`
  - `fun parseTreeSpec(json: String, name: String = "feed"): TreeSpec`

Mapping rules (must implement exactly):

| Input | Output kind |
|-------|-------------|
| `viewType == 1` (RecyclerView) | `LIST` |
| `viewType == 2` + `orientation == 0` | `ROW` |
| `viewType == 2` + `orientation == 1` | `COLUMN` |
| `layoutType == 1` (Stack) | `STACK` (overrides row/column) |
| `drawable.type == 1` (Text) and no meaningful children | Prefer `TEXT` with `props.text = drawable.data` |
| Image/Button/Icon drawable | `BOX` with background from props or default `0xFF888888` |
| `viewType == 3` | `BOX` (input not needed in bench; treat as empty box) |

Width/height: map `value`/`unit` (1=dp,2=px,3=percent). Padding/margin ints as-is. `gap` optional. Background color parse `#RRGGBB` / `#AARRGGBB` → Int (fallback null).

- [ ] **Step 1: Write fixture + failing test**

`sample_tree.json`:
```json
{
  "viewType": 2,
  "props": {
    "width": { "value": -1, "unit": 1 },
    "height": { "value": -2, "unit": 1 },
    "orientation": 1,
    "layoutType": 0,
    "gap": 8,
    "padding": { "left": 0, "top": 0, "right": 0, "bottom": 0 },
    "margin": { "left": 0, "top": 0, "right": 0, "bottom": 0 },
    "background": { "type": 1, "color": "#112233" }
  },
  "children": [
    {
      "viewType": 2,
      "props": {
        "width": { "value": -1, "unit": 1 },
        "height": { "value": -2, "unit": 1 },
        "orientation": 1,
        "layoutType": 0,
        "drawable": { "type": 1, "data": "Hello", "props": { "textSize": 14, "textColor": "#FFFFFF" } },
        "padding": { "left": 0, "top": 0, "right": 0, "bottom": 0 },
        "margin": { "left": 0, "top": 0, "right": 0, "bottom": 0 }
      },
      "children": []
    }
  ]
}
```

```kotlin
@Test
fun parses_column_with_text_child() {
    val json = javaClass.classLoader!!.getResourceAsStream("sample_tree.json")!!.reader().readText()
    val root = JsonTreeParser.parse(json)
    assertEquals(NodeKind.COLUMN, root.kind)
    assertEquals(1, root.children.size)
    assertEquals(NodeKind.TEXT, root.children[0].kind)
    assertEquals("Hello", root.children[0].props.text)
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement parser**

Use Gson `JsonObject` walking (more reliable than full typed graph for messy legacy JSON). Do not require every field present.

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/demo/jsontoview/parse app/src/test/java/com/demo/jsontoview/parse app/src/test/resources/sample_tree.json
git commit -m "feat(parse): map legacy view.json into FNode"
```

---

### Task 4: HierarchyStats + Timing utils

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/util/HierarchyStats.kt`
- Create: `app/src/main/java/com/demo/jsontoview/util/Timing.kt`
- Create: `app/src/test/java/com/demo/jsontoview/util/TimingTest.kt`

**Interfaces:**
- Produces:
  - `data class HierarchySnapshot(val viewCount: Int, val maxDepth: Int)`
  - `object HierarchyStats { fun snapshot(root: View): HierarchySnapshot }`
  - `object Timing { fun median(values: List<Double>): Double; fun nsToMs(ns: Long): Double }`

- [ ] **Step 1: Test median**

```kotlin
@Test
fun median_odd_and_even() {
    assertEquals(2.0, Timing.median(listOf(3.0, 1.0, 2.0)), 0.0)
    assertEquals(2.5, Timing.median(listOf(1.0, 2.0, 3.0, 4.0)), 0.0)
}
```

- [ ] **Step 2–4: Implement + pass + commit**

```bash
git commit -m "feat(util): timing median and view hierarchy stats"
```

`HierarchyStats.snapshot`: BFS/DFS; depth of root = 1; viewCount includes root.

---

### Task 5: FlatHostView (core flat renderer)

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/flat/FlatLayoutNode.kt` (mutable layout result: x,y,w,h + children)
- Create: `app/src/main/java/com/demo/jsontoview/flat/FlatHostView.kt`
- Modify: none of legacy required yet

**Interfaces:**
- Consumes: `FNode`, `Dimens`
- Produces:
  - `class FlatHostView(context: Context) : View(context)`
  - `fun bind(root: FNode)`
  - `fun clearTree()`
  - After measure/layout, fields used by benchmark: none extra; instrumentation from outside via measure/layout/draw overrides that record last durations is OK:
  - `var lastMeasureNs: Long`, `lastLayoutNs: Long`, `lastDrawNs: Long` (updated each pass)

Behavior:
- `bind` stores root, `requestLayout()`, `invalidate()`.
- `onMeasure`: recursive measure of virtual tree; `setMeasuredDimension`.
- ROW: sum child widths + gaps; height = max child.
- COLUMN: sum child heights + gaps; width = max child.
- STACK: size = max children; children at (padding + margin).
- TEXT: measure with `TextPaint` + `StaticLayout` (API 23+ Builder).
- BOX: use explicit size or min 16dp if wrap empty.
- LIST: if root/kind LIST, embed a `RecyclerView` as the only child of a thin `ViewGroup` wrapper — simpler approach: **make `FlatHostView` extend `FrameLayout`**, when kind LIST add `RecyclerView` filling parent; each item is a new `FlatHostView` bound to item node. For non-LIST trees, no RecyclerView child; all canvas.

Recommended structure:
```kotlin
class FlatHostView(context: Context) : FrameLayout(context) {
    private var root: FNode? = null
    private var layoutRoot: FlatLayoutNode? = null
    // if not LIST: setWillNotDraw(false); draw layoutRoot on canvas
    // if LIST: removeAllViews(); add RecyclerView
}
```

Fairness for LIST items: each row = one `FlatHostView` (canvas for that subtree).

- [ ] **Step 1: Implement FlatHostView + FlatLayoutNode** (no instrumentation test on JVM; manual later)

Minimal paint path for TEXT/BOX/ROW/COLUMN first; LIST second in same task.

- [ ] **Step 2: Compile**

Run: `./gradlew :app:compileDebugKotlin`  
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/demo/jsontoview/flat
git commit -m "feat(flat): FlatHostView canvas renderer for FNode trees"
```

---

### Task 6: NestedTreeBuilder + NestedHost

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/nested/NestedTreeBuilder.kt`
- Create: `app/src/main/java/com/demo/jsontoview/nested/NestedHost.kt`

**Interfaces:**
- Consumes: `FNode`, `Dimens`
- Produces:
  - `object NestedTreeBuilder { fun build(context: Context, node: FNode): View }`
  - `class NestedHost(context: Context) : FrameLayout(context) { fun bind(root: FNode); fun clearTree() }`

Mapping:
- ROW → `LinearLayout.HORIZONTAL`
- COLUMN → `LinearLayout.VERTICAL`
- STACK → `FrameLayout`
- TEXT → `TextView` (text, textSize, textColor)
- BOX → `View` with `GradientDrawable` color + radius
- LIST → `RecyclerView` + `LinearLayoutManager`; VH binds `NestedTreeBuilder.build` for item

LayoutParams: resolve width/height via `Dimens` + `MATCH_PARENT`/`WRAP_CONTENT`. Apply padding on the view; margin via `MarginLayoutParams`. Gap: add `topMargin`/`leftMargin` = gap on children index > 0.

- [ ] **Step 1: Implement builder + host**

- [ ] **Step 2: Compile** `./gradlew :app:compileDebugKotlin`

- [ ] **Step 3: Commit**

```bash
git commit -m "feat(nested): build real Android view trees from FNode"
```

---

### Task 7: BenchmarkReport + BenchmarkRunner

**Files:**
- Create: `app/src/main/java/com/demo/jsontoview/benchmark/RenderMode.kt`
- Create: `app/src/main/java/com/demo/jsontoview/benchmark/BenchmarkReport.kt`
- Create: `app/src/main/java/com/demo/jsontoview/benchmark/BenchmarkRunner.kt`
- Create: `app/src/test/java/com/demo/jsontoview/benchmark/BenchmarkReportTest.kt`

**Interfaces:**
- Consumes: `TreeSpec`, `FlatHostView`, `NestedHost`, `HierarchyStats`, `Timing`, main-thread `ViewGroup` container
- Produces:

```kotlin
enum class RenderMode { FLAT, NESTED }

data class BenchmarkReport(
    val mode: RenderMode,
    val treeName: String,
    val treeNodeCount: Int,
    val maxTreeDepth: Int,
    val measureMs: Double,
    val layoutMs: Double,
    val drawMs: Double,
    val firstFrameMs: Double,
    val scrollAvgFrameMs: Double?,
    val droppedFrames: Int?,
    val viewCount: Int,
    val maxViewDepth: Int,
    val javaHeapMb: Double,
    val pssMb: Double?,
)

class BenchmarkRunner(
    private val container: ViewGroup,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) {
    /**
     * Must be called on main thread for bind; heavy wait uses suspend or callback.
     * Protocol: 3 warm-up + 5 measured → median.
     */
    fun run(
        spec: TreeSpec,
        mode: RenderMode,
        includeScroll: Boolean = true,
        onComplete: (BenchmarkReport) -> Unit,
    )
}
```

Implementation notes:
- For each measured iteration: `container.removeAllViews()`, create host, `container.addView(host, MATCH, MATCH)`, `host.bind(spec.root)`, force `measure/layout` with exact specs from container size, record ns from host timers if available else wrap `host.measure`/`layout`.
- `firstFrameMs`: nanoTime from before addView until `host.viewTreeObserver.addOnDrawListener` first fire (remove listener after).
- `drawMs`: use single-shot `OnDrawListener` duration around `host.invalidate()` + wait one frame via `Choreographer.postFrameCallback`.
- Scroll: if tree root is LIST or height > container, fling RecyclerView/ScrollView; register `Choreographer.FrameCallback` for 1000ms; count frames with frameTimeNanos delta > 18_000_000 as dropped; avg delta ms.
- Memory: `val rt = Runtime.getRuntime(); javaHeapMb = (rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0`; `pssMb = Debug.getPss() / 1024.0`.
- Hierarchy: `HierarchyStats.snapshot(host)`.
- Log each report: `Log.i("FViewBench", report.toString())`.

Delta helper for UI:
```kotlin
fun deltaPct(nested: Double, flat: Double): Double =
    if (flat == 0.0) 0.0 else (nested - flat) / flat * 100.0
```

- [ ] **Step 1: Unit test delta/median usage on report helper** (pure)

- [ ] **Step 2: Implement runner (main-thread safe API with callbacks)**

- [ ] **Step 3: Compile**

- [ ] **Step 4: Commit**

```bash
git commit -m "feat(benchmark): runner collecting full layout/scroll/memory metrics"
```

---

### Task 8: BenchmarkActivity UI + layouts

**Files:**
- Create: `app/src/main/res/layout/activity_benchmark.xml`
- Create: `app/src/main/res/layout/item_benchmark_row.xml`
- Create: `app/src/main/java/com/demo/jsontoview/benchmark/BenchmarkActivity.kt`
- Create: `app/src/main/java/com/demo/jsontoview/benchmark/ResultsAdapter.kt`

**UI elements (activity_benchmark.xml):**
- `Spinner` or `RadioGroup` mode: Flat / Nested / Both
- `Spinner` source: Feed JSON / Shallow / Deep / Wide / FeedLike
- `Button` Run, `Button` Clear
- `FrameLayout` `@+id/benchContainer` (0dp weight 1)
- `RecyclerView` or `TableLayout` `@+id/resultsList` for reports
- `TextView` status line

**Behavior:**
- Load feed: `assets.open("view.json")` → `JsonTreeParser.parseTreeSpec`
- Synthetic: `SyntheticTreeFactory.*`
- Both: run Flat then Nested sequentially; show both rows + TextView delta summary for measureMs/layoutMs/viewCount
- Disable Run while running; re-enable on complete

- [ ] **Step 1: XML layouts**
- [ ] **Step 2: Activity + adapter**
- [ ] **Step 3: Compile**
- [ ] **Step 4: Commit**

```bash
git commit -m "feat(benchmark): BenchmarkActivity UI for presets and results"
```

---

### Task 9: Launcher + FeedActivity + Manifest

**Files:**
- Create: `app/src/main/res/layout/activity_launcher.xml`
- Create: `app/src/main/res/layout/activity_feed.xml`
- Create: `app/src/main/java/com/demo/jsontoview/demo/LauncherActivity.kt`
- Create: `app/src/main/java/com/demo/jsontoview/demo/FeedActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Manifest:**
- `LauncherActivity` = MAIN/LAUNCHER
- `FeedActivity`, `BenchmarkActivity` exported false
- Remove LAUNCHER from old `MainActivity` (delete or keep non-exported until Task 10)

**FeedActivity:** parse `view.json` on IO, bind `FlatHostView` full screen (LIST root expected).

**LauncherActivity:** two buttons → Feed / Benchmark.

- [ ] **Step 1: Implement activities + layouts + manifest**
- [ ] **Step 2: Install on emulator**

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

Expected: launcher shows; Feed paints list; Benchmark runs Deep preset without crash.

- [ ] **Step 3: Commit**

```bash
git commit -m "feat(demo): launcher and feed activity using FlatHostView"
```

---

### Task 10: Remove legacy code + fix deps

**Files (delete after nothing references them):**
- Old: `FView.kt`, `CustomViewGroup2.kt`, `MainActivity.kt`, `MainActivity2.kt`, `MyCustomAdapter.kt`, `ViewGroupType.kt`, `data.kt`, `Parser.kt`, old `drawable/*`, `handler/*`, `pattern/*`, `PropsLayout/*`, `UI/FViewComment.kt`, `helpers/*`, `models/*` (if replaced), `FileUtils.kt` (inline into demo if needed)

**Keep:** `assets/view.json`, app icons, themes, `AndroidManifest` permissions INTERNET optional (feed demo may not need if no images).

- [ ] **Step 1: Delete legacy sources; fix any leftover imports**
- [ ] **Step 2: Full test + assemble**

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git commit -m "refactor: remove legacy FView paths in favor of dual renderers"
```

---

### Task 11: Manual benchmark verification (emulator)

**Files:** none (runbook)

- [ ] **Step 1: Start emulator + install**

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

- [ ] **Step 2: Run matrix**

| Source | Mode | Record |
|--------|------|--------|
| Shallow | Both | measure/layout/viewCount |
| Deep | Both | measure/layout/viewCount |
| Wide | Both | measure/layout/viewCount |
| FeedLike | Both | + scroll metrics |
| Feed JSON | Both | smoke |

- [ ] **Step 3: Capture Logcat**

```bash
adb logcat -s FViewBench:I
```

Confirm Nested `viewCount` > Flat `viewCount` for deep trees; deep Nested measure/layout ≥ Flat order-of-magnitude story makes sense.

- [ ] **Step 4: Commit docs note if numbers worth saving**

Optional: append a short "Sample results" section to the design or a `docs/benchmark-notes.md` — only if runs succeed.

```bash
git commit -m "docs: note benchmark run procedure and sample observations"
```

---

## Spec coverage checklist

| Spec section | Task(s) |
|--------------|---------|
| Dual renderer architecture | 5, 6 |
| Shared FNode model | 1 |
| Synthetic presets | 2 |
| view.json parser | 3 |
| Full metrics + median protocol | 7 |
| Scroll jank | 7 |
| Memory heap/PSS + viewCount/depth | 4, 7 |
| Benchmark UI | 8 |
| Feed demo + launcher | 9 |
| No Glide in bench | 5–7 (BOX only) |
| Legacy cleanup | 10 |
| Manual verification | 11 |

## Type consistency notes

- Always `FNode` / `TreeSpec` / `NodeKind` / `RenderMode` / `BenchmarkReport` as defined in Tasks 1 and 7.
- Host APIs: `bind(root: FNode)` + `clearTree()` on both Flat and Nested.
- Log tag: `FViewBench` only.

---

## Execution handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-12-flat-vs-nested-benchmark.md`.

**Two execution options:**

1. **Subagent-Driven (recommended)** — fresh subagent per task, review between tasks  
2. **Inline Execution** — execute tasks in this session with checkpoints  

Which approach?
