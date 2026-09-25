plugins {
    id("common")
    application
}

application {
    mainClass.set("no.nav.dagpenger.vaktmester.mellomlagring.AppKt")
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.rapids.and.rivers)

    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation("no.nav.dagpenger:oauth2-klient:2026.09.24-18.22.358d5949ea39")
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.jackson)
    implementation("de.slub-dresden:urnlib:3.0.0")

    testImplementation(kotlin("test"))
    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
}
