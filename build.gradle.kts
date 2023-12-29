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
    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:2022.10.22-09.05.6fcf3395aa4f")
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.jackson)
    implementation("de.slub-dresden:urnlib:2.0.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
}
