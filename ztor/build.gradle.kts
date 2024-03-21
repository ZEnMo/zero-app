
plugins {
    //    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"

//    kotlin("jvm") version "1.9.20"
    id("io.ktor.plugin") version "2.3.4"
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

group = "com.zenmo"
version = "0.0.1"

application {
    mainClass.set("com.zenmo.ztor.ApplicationKt")

    // This argument is only applied when run through Gradle,
    // not when building and running a Fat Jar.
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

val ktor_version = "2.3.7"

dependencies {
    implementation(project(":zorm"))
    // not sure if/why we need to explicitly add Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")

    // for file upload
    implementation(platform("com.azure:azure-sdk-bom:1.2.18"))
    implementation("com.azure:azure-storage-blob:12.25.0")

    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.20")
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
