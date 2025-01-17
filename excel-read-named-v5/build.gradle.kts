plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.zenmo.excelreadnamed.v5"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi-ooxml:5.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.get()}")
    implementation(project(":zummon"))

    testImplementation(kotlin("test"))
}
//    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
//    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
//    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")


tasks {
    shadowJar {
        // Need to rename to prevent conflicts with other versions of Apache POI in AnyLogic
        //relocate("org.apache", "zenmo")
        //relocate("org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument", "zenmo.org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument"
    }

// This config breaks Ztor with error:
// """
// Your current kotlinx.serialization core version is 2.0.20,
// while current Kotlin compiler plugin unknown requires at least 1.0-M1-SNAPSHOT.
// Please update your kotlinx.serialization runtime dependency
// """
//    jar {
//        manifest {
//            attributes["Main-Class"] = "com.zenmo.excelreadnamed.MainKt"
//        }
//        from(
//            configurations
//                .runtimeClasspath
//                .get()
//                .filter { it.name.endsWith("jar") }
//                .map { zipTree(it) }
//        ) {
//            exclude("META-INF/versions/9/module-info.class")
//            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//        }
//    }
}
