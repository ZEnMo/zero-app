plugins {
    // need to specify version here because this plugin in used in multiple subprojects
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false

    // some tooling seems to prefer this format
    //    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply false
    //    id("org.jetbrains.kotlin.jvm") version "2.0.0" apply false
}
