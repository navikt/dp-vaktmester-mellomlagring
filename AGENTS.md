# AGENTS.md — navikt/dp-vaktmester-mellomlagring

## Repository Overview

Kafka-consumer (Rapids & Rivers) som rydder opp vedlegg lagret i `dp-mellomlagring` når en søknad
slettes. Ingen egen lagring/database — appen lytter på ett Kafka-event og kaller
`dp-mellomlagring`s HTTP-API for å liste og slette filer.

## Tech Stack

- Kotlin (JVM toolchain 25)
- Rapids & Rivers (`RapidApplication`, `River.PacketListener`) for Kafka-konsumering
- Ktor-klient mot `dp-mellomlagring`s vedlegg-API, med Azure AD client-credentials-token

## Build & Test Commands

```bash
./gradlew kt && ./gradlew build                                                       # Bygg
./gradlew test                                                                        # Kjør alle tester
./gradlew test --tests "no.nav.dagpenger.vaktmester.mellomlagring.VaktmesterTest"     # Kjør én testklasse
```

- Lint/format: `ktlint` kjøres automatisk (`ktlintFormat` er `dependsOn` for `KotlinCompile` i
  `buildSrc`) — ingen egen lint-kommando trengs før commit.

## Arkitektur

- **`App.kt`** — `object App` bootstrapper `RapidsConnection` og registrerer `Vaktmester` som
  eneste `River.PacketListener`.
- **`Vaktmester.kt`** — River som lytter på `@event_name = "søknad_slettet"`. For hver fil funnet
  via `MellomlagringClient.list(soknadUuid, ident)` kalles `MellomlagringClient.slett(urn, ident)`.
  Feil per fil logges, men `getOrThrow()` propagerer første feil og stopper videre behandling av
  den pakken.
- **`MellomlagringClient.kt`** — interface med én implementasjon (`MellomlagringHttpClient`) som
  kaller `dp-mellomlagring`s vedlegg-API (`GET /{soknadId}`, `DELETE /{urn}`). Bruker `X-Eier`-
  header for eierskap og Azure AD-token for autentisering. Interfacet finnes for å kunne mocke i
  tester.
- **`Configuration.kt`** — `object` med `by lazy` for delte ressurser (`azureAdClient`,
  `mellomlagringTokenSupplier`). Henter Azure AD client-credentials-token mot scopet til
  `dp-mellomlagring` via `CachedOauth2Client`.
- **`buildSrc`** — felles Gradle-konvensjoner (Kotlin-versjon, ktlint, test-logging) sentralisert i
  `common`-pluginen, ikke duplisert i rot-`build.gradle.kts`.

## Code Standards

- Domeneklasser er `internal`.
- Delt oppsett/konfig bruker `object` + `by lazy` (se `Configuration.kt`), ikke
  `companion object` med `@BeforeAll`/`@JvmStatic`.
- Tester bruker `TestRapid` for å simulere innkommende Kafka-meldinger og `mockk` for å verifisere
  kall mot `MellomlagringClient` (se `VaktmesterTest`).

## Deployment

- Plattform: Nais (Kubernetes på GCP)
- Manifester i `.nais/`
- Ingen HTTP-endepunkter å eksponere — ren Kafka-consumer

## Boundaries

### ✅ Always
- Følg `object` + `by lazy`-mønsteret for delt konfig/testoppsett
- Kjør tester før commit
- Behold `internal`-synlighet på domeneklasser

### ⚠️ Ask First
- Endringer i Kafka-konfigurasjon (`KAFKA_*`-nøkler i `Configuration.kt`)
- Endringer i Azure AD-oppsett/scope mot `dp-mellomlagring`
- Nye eksterne avhengigheter

### 🚫 Never
- Committe hemmeligheter/credentials
- Fange og svelge feil fra `MellomlagringClient` uten logging
