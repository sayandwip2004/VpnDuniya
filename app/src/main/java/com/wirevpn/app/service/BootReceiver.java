package com.wirevpn.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wirevpn.app.managers.ConfigManager;
import com.wirevpn.app.managers.ServerManager;
import com.wirevpn.app.models.Server;
import com.wireguard.config.Config;

/**
 * BootReceiver auto-reconnects to the last used VPN server
 * when the device boots (if the user had an active connection before).
 *
 * Requires RECEIVE_BOOT_COMPLETED permission in the manifest.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "Boot completed – checking for auto-reconnect...");

        // TODO: Check a SharedPreference flag "was_connected_before_boot"
        // For now, we skip auto-reconnect to avoid unexpected connections.
        // To enable: uncomment the block below and set the flag in
        // WireGuardVpnService when connecting/disconnecting.

        /*
        SharedPreferences prefs = context.getSharedPreferences("wirevpn_prefs", Context.MODE_PRIVATE);
        boolean wasConnected = prefs.getBoolean("was_connected", false);

        if (wasConnected) {
            ServerManager serverManager = ServerManager.getInstance(context);
            ConfigManager configManager = new ConfigManager(context);
            Server server = serverManager.getSelectedServer();

            if (server != null) {
                Config config = configManager.loadConfig(server);
                if (config != null) {
                    WireGuardVpnService vpnService = WireGuardVpnService.getInstance(context);
                    vpnService.connect(config);
                    Log.d(TAG, "Auto-reconnecting to: " + server.getName());
                }
            }
        }
        */
    }
}
