import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization")
}
group = "com.zenmo"
version = System.getenv("VERSION_TAG") ?: "dev"

repositories {
    maven("https://repo.osgeo.org/repository/release/")
    mavenCentral()
}

val ktor_version = "3.0.3"

dependencies {
    testImplementation(kotlin("test"))
    // Ztor is started in the test.
    testImplementation(project(":ztor"))
    testImplementation(project(":zorm"))
    testImplementation("org.jetbrains.exposed:exposed-core:${libs.versions.exposed.get()}")

    implementation(project(":zummon"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinx.serialization.json.get()}")

    // BAG stuff
    implementation("com.zenmo:bag:0.0.2")
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.zenmo"
            artifactId = "vallum"
            version = System.getenv("VERSION_TAG") ?: "dev"

            artifact(tasks["shadowJar"] as ShadowJar)
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/zenmo/zero-web")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
