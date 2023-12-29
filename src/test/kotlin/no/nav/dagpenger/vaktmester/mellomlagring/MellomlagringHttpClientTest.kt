package no.nav.dagpenger.vaktmester.mellomlagring

import de.slub.urn.URN
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.vaktmester.mellomlagring.MellomlagringClient.FilMetadata
import org.junit.jupiter.api.Test
import java.util.UUID

internal class MellomlagringHttpClientTest {
    private val testTokenSupplier = { "testToken" }
    private val baseUrl = "http://baseulr"
    private val uuid = UUID.randomUUID()
    private val testIdent = "123"

    @Test
    fun `riktig kall til mellomlagring og håndtering av OK svar`() {
        runBlocking {
            MellomlagringHttpClient(
                baseUrl,
                testTokenSupplier,
                MockEngine { request ->
                    request.url.toString() shouldBe "$baseUrl/$uuid"
                    request.headers[HttpHeaders.Authorization] shouldBe "Bearer ${testTokenSupplier.invoke()}"
                    request.headers["X-Eier"] shouldBe testIdent
                    request.method shouldBe HttpMethod.Get

                    respond(
                        content =
                            """[
                        {"filnavn": "filnavn1", "urn": "urn:vedlegg:id/sub/uuid1" },
                        {"filnavn": "filnavn2", "urn": "urn:vedlegg:id/sub/uuid2" }
                        ]
                            """.trimMargin(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ).list(uuid, testIdent).getOrThrow().let {
                it shouldBe
                    listOf(
                        FilMetadata("filnavn1", "urn:vedlegg:id/sub/uuid1"),
                        FilMetadata("filnavn2", "urn:vedlegg:id/sub/uuid2"),
                    )
            }
        }
    }

    @Test
    fun `Riktig slett kall til mellomlagring og håndtering av OK svar`() {
        runBlocking {
            MellomlagringHttpClient(
                baseUrl, testTokenSupplier,
                MockEngine { request ->
                    request.url.toString() shouldBe "$baseUrl/$uuid/hubba"
                    request.headers[HttpHeaders.Authorization] shouldBe "Bearer ${testTokenSupplier.invoke()}"
                    request.headers["X-Eier"] shouldBe testIdent
                    request.method shouldBe HttpMethod.Delete

                    respond(content = "", HttpStatusCode.NoContent)
                },
            ).slett(
                FilMetadata(
                    urnString = "urn:vedlegg:$uuid/hubba",
                    filnavn = "",
                ).urn(),
                testIdent,
            ).getOrThrow() shouldBe Unit
        }
    }

    @Test
    fun `Håndtering av feil situasjoner`() {
        runBlocking {
            MellomlagringHttpClient(baseUrl, testTokenSupplier, MockEngine { this.respondBadRequest() })
                .list(uuid, testIdent).isFailure shouldBe true

            MellomlagringHttpClient(baseUrl, testTokenSupplier, MockEngine { this.respondBadRequest() })
                .slett(URN.rfc8141().parse("urn:ns:nss"), testIdent).isFailure shouldBe true
        }
    }
}
