package no.nav.dagpenger.vaktmester.mellomlagring

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import mu.KotlinLogging
import no.nav.dagpenger.vaktmester.mellomlagring.Configuration.APP_NAME
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    App.start()
}

internal object App : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.create(Configuration.config)

    private val logger = KotlinLogging.logger { }

    init {
        try {
            rapidsConnection.register(this)
            Vaktmester(
                rapidsConnection = rapidsConnection,
                mellomlagringClient =
                    MellomlagringHttpClient(
                        baseUrl = Configuration.dpMellomlagringBaseUrl,
                        azureAdTokenProvider = Configuration.mellomlagringTokenSupplier,
                    ),
            )
        } catch (e: Exception) {
            logger.error(e) { "Kunne ikke initialisere $APP_NAME" }
            throw e
        }
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter $APP_NAME" }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        logger.info { "Stopper $APP_NAME" }
    }

    fun start() = rapidsConnection.start()
}
