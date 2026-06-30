plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.application")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val androidMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("org.xerial:sqlite-jdbc:3.45.1.0")
        }

        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.8.2")
            implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
            implementation(files("libs/eloviewhomesdk.aar"))
            implementation(files("libs/elo-peripherals-refresh-sdk-release.aar"))
        }
    }
}

android {
    namespace = "com.tmrestaurant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tmrestaurant"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

compose.desktop {
    application {
        mainClass = "com.tmrestaurant.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
            )
            packageName = "TMRestaurant"
            packageVersion = "1.0.0"
        }
    }
}
