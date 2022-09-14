package no.nav.dagpenger.vaktmester.mellomlagring

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    App.start()
}

internal object App : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.create(Configuration.config)

    init {
        rapidsConnection.register(this)
        Vaktmester(
            rapidsConnection = rapidsConnection,
            mellomlagringClient = MellomlagringHttpClient(
                baseUrl = Configuration.dpMellomlagringBaseUrl,
                azureAdTokenProvider = Configuration.mellomlagringTokenSupplier,
            )
        )
    }

    fun start() = rapidsConnection.start()
}
