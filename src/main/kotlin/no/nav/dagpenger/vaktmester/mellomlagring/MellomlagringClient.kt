package no.nav.dagpenger.vaktmester.mellomlagring

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import de.slub.urn.URN
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.jackson.jackson
import mu.KotlinLogging
import no.nav.dagpenger.vaktmester.mellomlagring.MellomlagringClient.FilMetadata
import java.util.UUID

internal interface MellomlagringClient {
    suspend fun list(
        soknaId: UUID,
        ident: String,
    ): Result<List<FilMetadata>>

    suspend fun slett(
        urn: URN,
        ident: String,
    ): Result<Unit>

    data class FilMetadata(
        val filnavn: String,
        @JsonProperty("urn")
        val urnString: String,
    ) {
        fun urn() = URN.rfc8141().parse(urnString)
    }
}

internal class MellomlagringHttpClient(
    private val baseUrl: String,
    private val azureAdTokenProvider: () -> String,
    engine: HttpClientEngine = CIO.create(),
) : MellomlagringClient {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val httpClient =
        HttpClient(engine) {
            expectSuccess = true
            defaultRequest {
                header("Authorization", "Bearer ${azureAdTokenProvider.invoke()}")
            }
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }

    private fun HttpRequestBuilder.addXEierHeader(eier: String) {
        this.header("X-Eier", eier)
    }

    override suspend fun list(
        soknaId: UUID,
        ident: String,
    ): Result<List<FilMetadata>> {
        val url = "$baseUrl/$soknaId"
        return kotlin.runCatching {
            httpClient.get(url) {
                addXEierHeader(ident)
            }.body<List<FilMetadata>>()
        }.onFailure { logger.error(it) { "Feilet GET mot $url" } }
    }

    override suspend fun slett(
        urn: URN,
        ident: String,
    ): Result<Unit> {
        val url = "$baseUrl/${urn.namespaceSpecificString()}"
        return kotlin.runCatching {
            httpClient.delete(url) { addXEierHeader(eier = ident) }
            Unit
        }
            .onFailure { logger.error(it) { "Feilet DELETE mot $url" } }
    }
}
