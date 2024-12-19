import java.net.URI

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "com.zenmo"
version = System.getenv("VERSION_TAG") ?: "dev"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
    }
    js(IR) {
        useEsModules()
        generateTypeScriptDefinitions()
        binaries.library()
        compilations["main"].packageJson {
            // hack hack hack
            types = "kotlin/zero-zummon.d.ts"
        }
        browser {
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${libs.versions.kotlinx.serialization.json.get()}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinx.serialization.json.get()}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                implementation("com.benasher44:uuid:0.8.4")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jsMain {
            dependencies {
                // align versions with frontend
                implementation(npm("@js-joda/core", "^5.6.3"))
                implementation(npm("@js-joda/timezone", "^2.21.1"))
            }
        }
    }
}
