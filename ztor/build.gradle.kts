
val ktor_version = "2.3.12"

plugins {
    kotlin("jvm")

    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization")
}

group = "com.zenmo"
version = "0.0.1"

application {
    mainClass.set("com.zenmo.ztor.ApplicationKt")

    // These arguments are only applied when running through Gradle (= in development),
    // not when building and running a Fat Jar (= in production).
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=true",
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":zorm"))
    implementation(project(":zummon"))

    // for file upload
    implementation(platform("com.azure:azure-sdk-bom:1.2.18"))
    implementation("com.azure:azure-storage-blob:12.25.0")

    // Explicitly add Exposed to be able to use the database object for dependency injection and transactions.
    implementation("org.jetbrains.exposed:exposed-core:${libs.versions.exposed.get()}")

    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    // kinda need this to run in Azure Container Apps
    // https://learn.microsoft.com/en-us/azure/container-apps/ingress-overview#http-headers
    implementation("io.ktor:ktor-server-forwarded-header:$ktor_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    // to hash deeplink secrets
    implementation("at.favre.lib:bcrypt:0.10.2")

    // to decode and validate Keycloak access tokens with user info.
    implementation("com.nimbusds:nimbus-jose-jwt:9.39.2")
    implementation("com.google.crypto.tink:tink:1.13.0")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.20-Beta2")
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
