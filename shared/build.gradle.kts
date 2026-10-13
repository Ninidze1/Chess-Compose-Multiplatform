import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ktlint)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    // The default hierarchy template (commonMain -> iosMain -> ios*Main) is applied
    // automatically, so the source sets need no manual dependsOn wiring.
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    // Compose Multiplatform no longer publishes iosX64 (Intel simulator) artifacts, so only
    // devices and Apple silicon simulators are targeted.
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        // PlatformUtils is an expect/actual class, which Kotlin still ships as Beta.
        freeCompilerArgs.add("-Xexpect-actual-classes")
        // FlowSettings and the DataStore/NSUserDefaults adapters are marked experimental
        // by multiplatform-settings.
        optIn.add("com.russhwolf.settings.ExperimentalSettingsApi")
        optIn.add("com.russhwolf.settings.ExperimentalSettingsImplementation")
    }

    cocoapods {
        summary = "Shared module of the Chess app: game rules, AI engine, persistence and the Compose UI"
        homepage = "https://github.com/Ninidze1/Chess-Compose-Multiplatform"
        authors = "Giorgi Ninidze"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
        // The podspec "resources" attribute is owned by the Compose Multiplatform plugin,
        // which points it at the synced composeResources (drawables and sounds).
    }

    sourceSets {
        commonMain.dependencies {
            // Exposed as api so the Android app module uses the same Compose stack.
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.material3)
            implementation(libs.compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            // Core Settings types (ObservableSettings) and opt-in markers, which the coroutines
            // and test artifacts only expose at runtime.
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
        }
        androidMain.dependencies {
            api(libs.koin.android)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.multiplatform.settings.datastore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // MapSettings, an in-memory Settings for the repository tests.
            implementation(libs.multiplatform.settings.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.ninidze.chesscomposekmm.resources"
}

android {
    namespace = "com.ninidze.chesscomposekmm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

ktlint {
    version.set(libs.versions.ktlint.cli.get())
    filter {
        // The ported third-party engine keeps its upstream formatting.
        exclude("**/domain/engine/**")
        // Generated sources, such as the Compose resource accessors.
        exclude { element -> element.file.invariantSeparatorsPath.contains("/build/") }
    }
}
