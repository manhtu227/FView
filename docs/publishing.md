# Publishing json-to-view

## Coordinates

| Channel | Coordinate |
|---------|------------|
| JitPack | `com.github.manhtu227.FView:json-to-view:<tag>` |
| Maven Central (target) | `io.github.manhtu227:json-to-view:<version>` |

## Local

```bash
./gradlew :json-to-view:publishToMavenLocal
```

## JitPack

1. Push a git tag (`v1.0.0`)
2. Open https://jitpack.io/#manhtu227/FView
3. Build the tag; consume with `maven { url = uri("https://jitpack.io") }`

## Maven Central (maintainer)

Requires a [Central Portal](https://central.sonatype.com/) account and GPG signing.

1. Set env (never commit secrets):

```bash
export ORG_GRADLE_PROJECT_mavenCentralUsername=...
export ORG_GRADLE_PROJECT_mavenCentralPassword=...
export ORG_GRADLE_PROJECT_signingKey=...
export ORG_GRADLE_PROJECT_signingPassword=...
```

2. Wire the [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) or Sonatype publish plugin in `json-to-view/build.gradle.kts` when credentials are available.

3. Publish:

```bash
./gradlew :json-to-view:publish
```

Until Central is configured, **JitPack + mavenLocal** are the supported public/dev installs. Module already has `maven-publish` for mavenLocal / JitPack install.
