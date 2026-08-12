# Sample app demo

The `:app` module is a **consumer** of `:json-to-view`, not part of the published AAR.

## Run

```bash
./gradlew :app:installDebug
adb shell am start -n com.demo.jsontoview/.demo.LauncherActivity
```

## Screens

### Launcher

- **Feed demo** — load `app/src/main/assets/view.json` on a background thread, bind with `JsonToViewHost` in **FLAT** mode (performance path).
- **Benchmark** — compare Flat vs Nested on the same `TreeSpec`.

### Feed demo

Shows a real-ish feed tree through the public SDK API:

```kotlin
JsonToViewHost(context).apply {
    mode = JsonToViewHost.Mode.FLAT
    bindJson(jsonFromAssets)
}
```

### Benchmark

1. Choose **Flat**, **Nested**, or **Both**
2. Choose source: Feed JSON / Shallow / Deep / Wide / FeedLike
3. Tap **Run**
4. Inspect rows in the results list and Logcat:

```bash
adb logcat -s FViewBench:I
```

Protocol (library): 3 warm-up + 5 measured runs → median.  
See [benchmark-notes.md](benchmark-notes.md) for sample device numbers.

## Building as an external app would

After publishing the library (JitPack or `mavenLocal`), a separate app only needs the dependency and the same `JsonToViewHost` calls — no copy of library sources.
