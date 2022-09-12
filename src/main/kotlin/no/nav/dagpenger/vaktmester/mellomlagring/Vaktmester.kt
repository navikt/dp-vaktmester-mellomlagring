package no.nav.dagpenger.vaktmester.mellomlagring

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class Vaktmester(rapidsConnection: RapidsConnection, private val mellomlagringClient: MellomlagringClient) :
    River.PacketListener {
    companion object {
        private val logg = KotlinLogging.logger {}
        const val BEHOV = "HUBBA" // todo
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(BEHOV)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("søknad_uuid", "ident") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val ident = packet.ident()
        val soknaId = packet.søknadUuid()
        runBlocking {
            kotlin.runCatching {
                mellomlagringClient.list(soknaId, ident)
                    .also { logg.info { "Skal slette ${it.size} filer for søknad: $soknaId " } }
                    .forEach { r ->
                        mellomlagringClient.slett(r.urn, ident).also {
                            logg.info { "Slettet fil: ${r.filnavn}" }
                        }
                    }
            }.fold(
                onSuccess = {},
                onFailure = {
                    logg.error(it) {
                        "Feil ved sletting av filer for søknadId: $soknaId"
                    }
                }
            )
        }
    }

    private fun JsonMessage.ident() = this["ident"].asText()
    private fun JsonMessage.søknadUuid(): UUID = this["søknad_uuid"].asText().let { UUID.fromString(it) }
}
