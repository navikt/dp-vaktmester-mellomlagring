package no.nav.dagpenger.vaktmester.mellomlagring

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.UUID

internal class Vaktmester(rapidsConnection: RapidsConnection, private val mellomlagringClient: MellomlagringClient) :
    River.PacketListener {
    companion object {
        private val logg = KotlinLogging.logger {}
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "søknad_slettet") }
            validate { it.requireKey("søknad_uuid", "ident") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
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
