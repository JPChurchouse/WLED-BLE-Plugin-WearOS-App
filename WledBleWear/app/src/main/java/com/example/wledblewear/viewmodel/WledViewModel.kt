package com.example.wledble.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.tiles.TileService
import com.example.wledble.WledApplication
import com.example.wledble.data.PreferencesKeys
import com.example.wledble.data.wledDataStore
import com.example.wledble.model.Preset
import com.example.wledble.model.WledUiState
import com.example.wledble.tile.WledTileService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WledViewModel(application: Application) : AndroidViewModel(application) {

    private val app        = application as WledApplication
    private val bleManager = app.bleManager            // shared singleton
    private val dataStore  = application.wledDataStore
    private val json       = Json { ignoreUnknownKeys = true }

    val uiState: StateFlow<WledUiState> = bleManager.state

    init {
        // Mirror BLE state into DataStore so the tile can read it offline.
        viewModelScope.launch {
            uiState.collect { state ->
                dataStore.edit { prefs ->
                    prefs[PreferencesKeys.IS_POWERED] = state.isPowered
                    state.activePresetId?.let { prefs[PreferencesKeys.ACTIVE_PRESET] = it }
                    if (state.presets.isNotEmpty()) {
                        prefs[PreferencesKeys.PRESETS_JSON] =
                            json.encodeToString<List<Preset>>(state.presets)
                    }
                }
                // Tell the tile it should redraw. Fire-and-forget; ignore the Future.
                TileService.getUpdater(app).requestUpdate(WledTileService::class.java)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        // Persist device info so tile can reconnect without scanning.
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.DEVICE_MAC]  = device.address
                prefs[PreferencesKeys.DEVICE_NAME] = device.name ?: "WLED Device"
            }
        }
        bleManager.connect(device)
    }

    fun startScan()             = bleManager.startScan()
    fun disconnect()            = bleManager.disconnect()
    fun togglePower()           = bleManager.writePower(!uiState.value.isPowered)
    fun activatePreset(id: Int) = bleManager.writeActivePreset(id)

    override fun onCleared() {
        super.onCleared()
        // Do NOT call bleManager.cleanup() — the BLE connection must outlive
        // the ViewModel so the tile can operate without re-connecting.
        // The Application process manages the BleManager lifetime.
    }
}