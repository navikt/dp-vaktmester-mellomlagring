package no.nav.dagpenger.vaktmester.mellomlagring

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
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.jackson.jackson
import no.nav.dagpenger.vaktmester.mellomlagring.MellomlagringClient.MellomlagringException
import no.nav.dagpenger.vaktmester.mellomlagring.MellomlagringClient.Response
import java.util.UUID

internal interface MellomlagringClient {
    suspend fun list(soknaId: UUID, ident: String): List<Response>
    suspend fun slett(urn: URN, ident: String)

    data class Response(
        val filnavn: String,
        val urn: URN,
    )

    class MellomlagringException(msg: String, t: Throwable) : RuntimeException(msg, t)
}

internal class MelllomlagringHttpClient(
    private val baseUrl: String,
    private val azureAdTokenProvider: () -> String,
    engine: HttpClientEngine = CIO.create()
) : MellomlagringClient {
    private val httpClient = HttpClient(engine) {
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

    override suspend fun list(soknaId: UUID, ident: String): List<Response> {
        val url = "$baseUrl/$soknaId"
        return kotlin.runCatching {
            httpClient.get(url).body<List<Response>>()
        }.fold(
            onSuccess = { it },
            onFailure = { t -> throw MellomlagringException("Feilet GET mot $url", t) }
        )
    }

    override suspend fun slett(urn: URN, ident: String) {
        val url = "$baseUrl/${urn.namespaceSpecificString()}"
        return kotlin.runCatching {
            httpClient.delete(url)
        }.fold(
            onSuccess = {},
            onFailure = { t -> throw MellomlagringException("Feiltet DELETE mot $url", t) }
        )
    }
}
