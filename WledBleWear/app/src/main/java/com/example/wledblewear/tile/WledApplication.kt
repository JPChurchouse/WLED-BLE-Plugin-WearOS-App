package com.example.wledble

import android.app.Application
import com.example.wledble.ble.BleManager

/**
 * Application subclass that holds the BleManager singleton.
 *
 * Lifetime: created with the process, destroyed when the OS kills the process.
 * Both [MainActivity] (via WledViewModel) and [WledTileService] share the
 * same [BleManager] instance, so an active GATT connection established in the
 * app is immediately usable from the tile without re-scanning.
 */
class WledApplication : Application() {
    val bleManager: BleManager by lazy { BleManager(this) }
}