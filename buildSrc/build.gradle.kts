plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "7.2.1"
    kotlin("jvm") version "2.2.21"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.23.3")
}
