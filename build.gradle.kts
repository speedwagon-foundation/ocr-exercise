plugins {
    kotlin("jvm") version "1.9.23"
}

group = "hofwimmer.lukas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.openpnp:opencv:4.9.0-0")
    implementation("net.imagej:ij:1.54i")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}