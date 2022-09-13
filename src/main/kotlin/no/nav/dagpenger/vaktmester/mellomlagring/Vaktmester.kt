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
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "søknad_slettet") }
            validate { it.requireKey("søknad_uuid", "ident") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val ident = packet.ident()
        val soknaId = packet.søknadUuid()
        runBlocking {
            mellomlagringClient.list(soknaId, ident)
                .onFailure { logg.error(it) { "Feil i henting av filer for $soknaId" } }
                .onSuccess { logg.info { "Skal slette ${it.size} filer for søknad: $soknaId " } }
                .getOrThrow()
                .forEach { fil ->
                    mellomlagringClient.slett(fil.urn(), ident)
                        .onFailure { logg.error(it) { "Kunne ikke slette $fil" } }
                        .onSuccess { logg.info { "Slettet fil $fil" } }
                        .getOrThrow()
                }
        }
    }

    private fun JsonMessage.ident() = this["ident"].asText()
    private fun JsonMessage.søknadUuid(): UUID = this["søknad_uuid"].asText().let { UUID.fromString(it) }
}
