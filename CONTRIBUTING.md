# Contributing

Thanks for helping improve **json-to-view**.

## Prerequisites

- JDK 17  
- Android SDK (compile SDK 35, min SDK 24)  

```bash
./gradlew :json-to-view:testDebugUnitTest :app:assembleDebug
```

## Layout

| Module | Purpose |
|--------|---------|
| `:json-to-view` | Library API and renderers — prefer changes here |
| `:app` | Sample / demo only |

SDK package: `com.manhtu.jsontoview`.

## Guidelines

1. **Public API** — Prefer additive changes to `JsonToViewHost`, `JsonToView`, `FNode`, and both hosts. Document breaking changes clearly in `0.x`.
2. **Offline core** — Do not add network or Glide to measure/layout/draw hot paths. Image leaves stay color `BOX` unless behind an explicit app-provided loader API later.
3. **One model** — Flat and Nested must accept the same `FNode` tree.
4. **Tests** — Update or add JVM tests under `json-to-view/src/test` for model/parser/util changes.
5. **Sample app** — Keep demo UI thin; logic belongs in the library.

## Pull requests

- Describe what changed and why  
- Ensure unit tests pass locally  
- Avoid committing IDE files, `local.properties`, or build outputs  
