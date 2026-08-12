// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

// JitPack (and local checks) often invoke a root "install" task.
tasks.register("install") {
    dependsOn(":json-to-view:publishToMavenLocal")
    group = "publishing"
    description = "Publish :json-to-view to mavenLocal (JitPack-compatible entrypoint)"
}