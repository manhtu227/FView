# Public API freeze (1.0)

SemVer **1.x**: no breaking changes to the surface below without a new major version.

## Public packages

| Package | Types |
|---------|--------|
| `com.manhtu.jsontoview` | `JsonToViewHost`, `JsonToView`, `RenderConfig` |
| `com.manhtu.jsontoview.model` | `FNode`, `NodeKind`, `NodeProps`, `NodeAction`, `Dimension`, `DimUnit`, `EdgeInsets`, `TreeSpec`, `TreeSource`, `SyntheticTreeFactory`, `nodeCount`, `maxDepth` |
| `com.manhtu.jsontoview.parse` | `JsonTreeParser` |
| `com.manhtu.jsontoview.flat` | `FlatHostView` |
| `com.manhtu.jsontoview.nested` | `NestedHost`, `NestedTreeBuilder` |
| `com.manhtu.jsontoview.image` | `ImageLoader`, `ImageTarget` |
| `com.manhtu.jsontoview.action` | `NodeActionHandler` |
| `com.manhtu.jsontoview.benchmark` | `BenchmarkRunner`, `BenchmarkReport`, `RenderMode`, `deltaPct` (**tooling**, stable) |
| `com.manhtu.jsontoview.util` | `Dimens`, `Timing`, `HierarchyStats` |

## Compatibility promise

- Additive methods/fields with defaults are allowed in 1.x minors.
- Removing or renaming public types/methods requires **2.0**.
- Stable JSON mapping: see [MAPPING_STABLE.md](MAPPING_STABLE.md).
- Legacy `viewType` JSON remains supported through 1.x (deprecated for new backends).

## Non-goals (not API)

- Sample `:app` and `:consumer-demo` modules
- Glide (app-only)
