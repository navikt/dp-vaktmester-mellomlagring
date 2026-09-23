# dp-vaktmester-mellomlagring

Kafka-consumer (Rapids & Rivers) som rydder opp vedlegg i `dp-mellomlagring` når en søknad slettes.
Ingen egen lagring eller database — appen lytter på et event og kaller `dp-mellomlagring`s HTTP-API.

## Build, test og lint

- Bygg: `./gradlew build` (kjør `./gradlew kt` først, se README)
- Kjør alle tester: `./gradlew test`
- Kjør én testklasse: `./gradlew test --tests "no.nav.dagpenger.vaktmester.mellomlagring.VaktmesterTest"`
- Lint/format: ktlint kjøres automatisk (`ktlintFormat` er `dependsOn` for `KotlinCompile` i
  `buildSrc/src/main/kotlin/common.gradle.kts`), ingen egen lint-kommando trengs før commit.

## Arkitektur

- **`App.kt`** — `object App` bootstrapper `RapidsConnection` via `RapidApplication.create(Configuration.config)`
  og registrerer `Vaktmester` som eneste `River.PacketListener`.
- **`Vaktmester.kt`** — River som lytter på events med `@event_name = "søknad_slettet"`, henter
  `søknad_uuid`/`ident` fra pakken, og for hver fil funnet via `MellomlagringClient.list(...)`
  kalles `MellomlagringClient.slett(...)`. Feil logges per fil (ikke fatalt for hele batchen), men
  `getOrThrow()` gjør at first failure i en gitt operasjon propagerer og stopper videre behandling
  av den pakken.
- **`MellomlagringClient.kt`** — `MellomlagringHttpClient` er eneste implementasjon; kaller
  `dp-mellomlagring`s vedlegg-API (`GET /{soknadId}` og `DELETE /{urn}`) med Azure AD-token
  (`X-Eier`-header for eierskap, `Authorization`-header for autentisering). Interfacet finnes for
  å kunne mocke i tester (se `VaktmesterTest`).
- **`Configuration.kt`** — `object` med `by lazy` for delte ressurser (`azureAdClient`,
  `mellomlagringTokenSupplier`), ikke companion object/`@JvmStatic`. Henter Azure AD
  client-credentials-token mot scopet til `dp-mellomlagring` via `CachedOauth2Client`.

## Konvensjoner

- Domeneklasser er `internal`.
- Delt testoppsett/konfig bruker `object` + `by lazy` (se `Configuration.kt`), ikke
  `companion object` med `@BeforeAll`/`@JvmStatic` — følg dette mønsteret i nye tester.
- Tester bruker `TestRapid` fra `rapids_and_rivers_test` for å simulere innkommende Kafka-meldinger,
  og `mockk` for å verifisere kall mot `MellomlagringClient` (se `VaktmesterTest`).
- Felles Gradle-konvensjoner (Kotlin-versjon, ktlint, test-logging) er sentralisert i
  `buildSrc`-pluginen `common`, ikke duplisert i rot-`build.gradle.kts`.
