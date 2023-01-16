package api

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModuleFactory
import com.webasyst.api.Installation
import com.webasyst.api.WAIDAuthenticator

class PhotoApiClientFactory(
    private val config: ApiClientConfiguration,
    private val waidAuthenticator: WAIDAuthenticator,
) : ApiModuleFactory<PhotoApiClient>() {
    override val scope = PhotoApiClient.SCOPE

    override fun instanceForInstallation(installation: Installation): PhotoApiClient {
        return PhotoApiClient(
            config = config,
            waidAuthenticator = waidAuthenticator,
            installation = installation,
        )
    }
}
