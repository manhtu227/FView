plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.manhtu.jsontoview"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.gson)

    testImplementation(libs.junit)
}

group = "io.github.manhtu227"
version = "0.1.0"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "io.github.manhtu227"
                artifactId = "json-to-view"
                version = project.version.toString()

                pom {
                    name.set("json-to-view")
                    description.set(
                        "Android SDK: render declarative JSON/UI trees with Flat (canvas) or Nested (View hierarchy) backends.",
                    )
                    url.set("https://github.com/manhtu227/FView")
                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("manhtu227")
                            name.set("manhtu227")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/manhtu227/FView.git")
                        developerConnection.set("scm:git:ssh://github.com/manhtu227/FView.git")
                        url.set("https://github.com/manhtu227/FView")
                    }
                }
            }
        }
        repositories {
            mavenLocal()
        }
    }
}
