package no.nav.dagpenger.vaktmester.mellomlagring

import de.slub.urn.URN
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import no.nav.dagpenger.vaktmester.mellomlagring.MellomlagringClient.FilMetadata
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.util.UUID

internal class VaktmesterTest {
    private val testRapid = TestRapid()
    private val uuid = UUID.randomUUID()
    private val testIdent = "123"
    private val urn = URN.rfc8141().parse("urn:vedlegg:filnavn1")

    @Test
    fun `Skal håndtere pakker som oppfyller krav`() {
        val mellomlagringClientMock =
            mockk<MellomlagringClient>().also {
                coEvery { it.list(uuid, testIdent) } returns
                    Result.success(
                        listOf(
                            FilMetadata(
                                "filnavn1",
                                "urn:vedlegg:filnavn1",
                            ),
                        ),
                    )
                coEvery { it.slett(urn, testIdent) } returns Result.success(Unit)
            }
        Vaktmester(rapidsConnection = testRapid, mellomlagringClient = mellomlagringClientMock).also {
            testRapid.sendTestMessage(
                """
                 {
                  "@event_name": "søknad_slettet",
                  "søknad_uuid": "$uuid",
                  "ident": "$testIdent"
                } 
                """.trimIndent(),
            )
        }

        coVerify(exactly = 1) {
            mellomlagringClientMock.list(uuid, testIdent)
            mellomlagringClientMock.slett(urn, testIdent)
        }
    }
}
