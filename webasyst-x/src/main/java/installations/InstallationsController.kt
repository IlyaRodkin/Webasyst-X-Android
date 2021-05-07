package com.webasyst.x.installations

import android.util.Log
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.api.webasyst.WebasystApiClientFactory
import com.webasyst.waid.WAIDClient
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.cache.DataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object InstallationsController {
    private val waidClient: WAIDClient = WebasystXApplication.instance.waidClient
    private val apiClient = WebasystXApplication.instance.apiClient
    private val webasystApiClientFactory = (apiClient.getFactory(WebasystApiClient::class.java) as WebasystApiClientFactory)
    private val dataCache: DataCache = WebasystXApplication.instance.dataCache

    private val mutableInstallations = MutableStateFlow(dataCache.readInstallationList()?.also {
        Log.d(TAG, "Loaded ${it.size} installations from local storage")
    } ?: null.also { Log.d(TAG, "Did not load any installations from local storage") })
    val installations: StateFlow<List<Installation>?>
        get() = mutableInstallations

    private val mutableCurrentInstallation = MutableStateFlow(installations.value?.firstOrNull())
    val currentInstallation: StateFlow<Installation?>
        get() = mutableCurrentInstallation

    fun clearInstallations() {
        mutableInstallations.value = null
        dataCache.clearInstallationList()
        mutableCurrentInstallation.value = null
    }

    fun updateInstallations() {
        GlobalScope.launch(Dispatchers.IO) {
            var selectedInstallation = currentInstallation.value?.id
            Log.d(TAG, "Updating installations...")
            val installationsResponse = waidClient.getInstallationList()
            if (installationsResponse.isFailure()) {
                Log.d(TAG, "Failed to update installations", installationsResponse.getFailureCause())
                return@launch
            }
            val installations = installationsResponse.getSuccess().map {
                Installation(it)
            }
            Log.d(TAG, "Loaded ${installations.size} installations from WAID")
            mutableInstallations.value = installations
            if (installations.isNotEmpty() && selectedInstallation == null) {
                selectedInstallation = installations.first().id
            }
            restoreSelection(installations, selectedInstallation)
            dataCache.storeInstallationList(installations)
            Log.d(TAG, "Saved ${installations.size} installations to local storage")
            val namedInstallations = installations
                .associateWith { installation ->
                    async {
                        webasystApiClientFactory
                            .instanceForInstallation(installation)
                            .getInstallationInfo()
                    }
                }
                .map { (installation, asyncInfo) ->
                    val infoResponse = asyncInfo.await()
                    if (infoResponse.isSuccess()) {
                        val info = infoResponse.getSuccess()
                        installation.copy(
                            name = info.name,
                            icon = Installation.Icon(info),
                        )
                    } else {
                        installation
                    }
                }
            restoreSelection(namedInstallations, selectedInstallation)
            dataCache.storeInstallationList(namedInstallations)
            Log.d(TAG, "Saved ${installations.size} augmented installations to local storage")
            mutableInstallations.value = namedInstallations
        }
    }

    fun setSelectedInstallation(id: String?) {
        restoreSelection(installations?.value, id)
    }

    fun setSelectedInstallation(installation: Installation) {
        mutableCurrentInstallation.value = installation
    }

    private fun restoreSelection(installations: List<Installation>?, selected: String?) {
        mutableCurrentInstallation.value = if (selected == null) {
            null
        } else {
            installations?.firstOrNull {
                it.id == selected
            } ?: installations?.firstOrNull()
        }
    }

    const val TAG = "InstallationsController"
}