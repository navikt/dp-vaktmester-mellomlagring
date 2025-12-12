plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.21"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("org.jlleitschuh.gradle:ktlint-gradle:14.0.1")
}
