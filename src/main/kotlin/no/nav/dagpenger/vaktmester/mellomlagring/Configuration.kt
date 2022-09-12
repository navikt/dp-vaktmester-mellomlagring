package no.nav.dagpenger.vaktmester.mellomlagring

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

internal object Configuration {

    const val appName = "dp-vaktmester-mellomlagring"

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "RAPID_APP_NAME" to appName,
            "KAFKA_CONSUMER_GROUP_ID" to "$appName-v1",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "DP_MELLOMLAGRING_BASE_URL" to "http://dp-mellomlagring/v1/azuread/mellomlagring/vedlegg",
            "DP_MELLOMLAGRING_SCOPE" to "api://dev-gcp.teamdagpenger.dp-mellomlagring/.default",
        )
    )

    val properties: Configuration =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val dpMellomlagringBaseUrl = properties[Key("DP_MELLOMLAGRING_BASE_URL", stringType)]

    val mellomlagringTokenSupplier: () -> String by lazy {
        azureAdTokenSupplier(properties[Key("DP_MELLOMLAGRING_SCOPE", stringType)])
    }

    val config: Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }

    private val azureAdClient: CachedOauth2Client by lazy {
        val azureAdConfig = OAuth2Config.AzureAd(properties)
        CachedOauth2Client(
            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
            authType = azureAdConfig.clientSecret()
        )
    }

    private fun azureAdTokenSupplier(scope: String): () -> String = {
        runBlocking { azureAdClient.clientCredentials(scope).accessToken }
    }
}
