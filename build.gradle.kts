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
    implementation("no.nav.dagpenger:oauth2-klient:2025.08.20-08.53.9250ac7fbd99")
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.jackson)
    implementation("de.slub-dresden:urnlib:2.0.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
}
