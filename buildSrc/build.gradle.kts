plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.20"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("org.jlleitschuh.gradle:ktlint-gradle:14.0.1")
}
