plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.zenmo.fudura"
version = "dev"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.get()}")

    implementation(platform("org.http4k:http4k-bom:5.31.1.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-kotlinx-serialization")

    // Http4k documentation prefers Apache above the Java built-in client but it seems fine
    //implementation("org.http4k:http4k-client-apache")
}

tasks.test {
    useJUnitPlatform()
}
