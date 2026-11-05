plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "me.padi.qqlite.revived"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "me.padi.qqlite.revived"
        // Miuix blur / glass uses Android RuntimeShader (API 33+).
        minSdk = 33
        targetSdk = 37
        versionCode = 102
        versionName = "1.0.2"

        ndk {
            abiFilters.addAll(
                arrayOf(
                  "armeabi-v7a",
                 "arm64-v8a"
                )
            )
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            signingConfig = signingConfigs["debug"]
        }
    }

    // AGP 9 built-in Kotlin does not compile custom Java source directories as Kotlin.
    sourceSets.named("main") {
        kotlin.directories.add(
            rootProject.file("third_party/miuix-glass/src/commonMain/kotlin").absolutePath
        )
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
            excludes += "**"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
    implementation(libs.material)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.okhttp)
    implementation(libs.zoomimage.compose.coil3)
    implementation(libs.graphics.shapes)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.savedstate)
    implementation(libs.material.icons.extended)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.viewmodel.navigation)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.squircle)
    implementation(libs.miuix.blur)
    implementation("io.github.kyant0:shapes:1.2.0")

    testImplementation("junit:junit:4.13.2")

}
