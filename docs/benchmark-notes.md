# Benchmark sample results

**Device:** Pixel 3 XL (crosshatch), Android 12  
**Build:** `feature/flat-vs-nested-benchmark` dual-renderer rewrite  
**Protocol:** 3 warm-up + 5 measured → median; log tag `FViewBench`  
**Date:** 2026-08-12  

## Matrix (Mode = Both)

| Source | Mode | nodes | depth | measureMs | layoutMs | drawMs | firstFrameMs | views | viewDepth | heapMB | notes |
|--------|------|------:|------:|----------:|---------:|-------:|-------------:|------:|----------:|-------:|-------|
| Shallow | FLAT | 57 | 3 | 4.89 | 1.02 | 4.34 | 10.4 | **1** | 1 | 5.4 | |
| Shallow | NESTED | 57 | 3 | 0.88 | 0.12 | 0.15 | 13.8 | **58** | 4 | 7.5 | |
| Deep | FLAT | 20 | 20 | 1.24 | 0.48 | 12.9 | 14.8 | **1** | 1 | 10.8 | |
| Deep | NESTED | 20 | 20 | 0.19 | 0.06 | 11.3 | 14.8 | **21** | 21 | 11.3 | hierarchy depth ≈ tree |
| Wide | FLAT | 200 | 2 | 16.3 | 1.62 | 0.18 | 18.3 | **1** | 1 | 12.7 | |
| Wide | NESTED | 200 | 2 | 3.33 | 0.31 | 0.14 | **45.5** | **201** | 3 | 20.4 | firstFrame / heap cost |
| FeedLike | FLAT | 946 | 7 | 41.8 | 4.90 | 0.18 | 47.1 | **1** | 1 | 13.4 | |
| FeedLike | NESTED | 946 | 7 | 29.5 | 1.52 | 0.19 | **164.9** | **947** | 8 | 22.4 | |
| Feed JSON | FLAT | 416 | 8 | 0.05* | 1.59 | 12.3 | 14.8 | 4 | 3 | 22.9 | LIST+RV; scrollAvg≈16.7ms, dropped=0 |
| Feed JSON | NESTED | 416 | 8 | 0.03* | 5.65 | 9.36 | 15.5 | **50** | 9 | 24.0 | scrollAvg≈16.7ms, dropped=0 |

\* LIST roots measure mostly the host shell; item work shows up in firstFrame / scroll.

## Observations

1. **viewCount:** Nested ≈ `treeNodeCount + 1` (host); Flat non-LIST stays at **1** host view — primary fairness proof that both renderers consume the same `TreeSpec`.
2. **maxViewDepth:** Nested tracks tree depth (deep: 21); Flat stays 1 for canvas trees.
3. **Memory:** Nested heap/PSS higher as node count grows (wide/feedLike).
4. **firstFrameMs:** Nested often worse on large trees (wide 45ms vs 18ms; feedLike 165ms vs 47ms) even when Nested platform measure/layout ns look small — allocation + hierarchy attach dominates.
5. **measureMs on pure canvas trees:** Flat recursive Kotlin measure can exceed Nested `LinearLayout` measure for medium trees; interpret with firstFrame + hierarchy + memory, not measure alone.
6. **Scroll:** Feed JSON LIST path collects `scrollAvgFrameMs` / `droppedFrames` for both modes.

## How to re-run

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
# Benchmark → Mode Both → pick source → Run
adb logcat -s FViewBench:I
```
