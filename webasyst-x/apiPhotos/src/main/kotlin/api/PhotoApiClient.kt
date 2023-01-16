package api

import com.webasyst.api.*
import models.Photos

class PhotoApiClient (
    config: ApiClientConfiguration,
    installation: Installation,
    waidAuthenticator: WAIDAuthenticator,
    ) : ApiModule(
    config = config,
    installation = installation,
    waidAuthenticator = waidAuthenticator,
    ) {
        override val appName get() = SCOPE

        suspend fun getPhotoList(): Response<Photos> = apiRequest {
            return client.doGet("$urlBase/api.php/photos.photo.getList")
        }

        companion object {
            const val SCOPE = "photos"
        }
    }

