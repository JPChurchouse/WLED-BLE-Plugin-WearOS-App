package com.example.wledble.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

/**
 * Top-level extension property — the DataStore delegate ensures a single
 * instance per process regardless of which Context accesses it.
 */
val Context.wledDataStore: DataStore<Preferences> by preferencesDataStore(name = "wled_prefs")

object PreferencesKeys {
    /** MAC address of the last successfully connected device. */
    val DEVICE_MAC    = stringPreferencesKey("device_mac")
    /** Display name of the last connected device. */
    val DEVICE_NAME   = stringPreferencesKey("device_name")
    /** Last known power state. Optimistically updated by tile; confirmed by BLE notify. */
    val IS_POWERED    = booleanPreferencesKey("is_powered")
    /** Full preset list as JSON — same schema as the BLE characteristic. */
    val PRESETS_JSON  = stringPreferencesKey("presets_json")
    /** Last known active preset ID. */
    val ACTIVE_PRESET = intPreferencesKey("active_preset_id")
}