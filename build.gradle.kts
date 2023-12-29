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
    implementation(libs.dp.biblioteker.oauth2.klient)
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.jackson)
    implementation("de.slub-dresden:urnlib:2.0.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
}
