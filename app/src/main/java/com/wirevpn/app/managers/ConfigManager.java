package com.wirevpn.app.managers;

import android.content.Context;
import android.util.Log;

import com.wirevpn.app.models.Server;
import com.wireguard.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * ConfigManager is responsible for loading WireGuard configuration files
 * from the app's assets directory and parsing them into Config objects.
 *
 * Future: Add support for downloading configs from a remote URL.
 */
public class ConfigManager {

    private static final String TAG = "ConfigManager";

    private final Context context;

    public ConfigManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Loads and parses a WireGuard Config for the given server.
     *
     * @param server The server to load the config for.
     * @return Parsed {@link Config} or null on failure.
     */
    public Config loadConfig(Server server) {
        if (server == null) {
            Log.e(TAG, "loadConfig: server is null");
            return null;
        }

        if (server.isRemoteConfig()) {
            // Future: download from URL
            Log.w(TAG, "Remote configs not yet supported. Server: " + server.getId());
            return null;
        }

        return loadConfigFromAssets(server.getConfigFile());
    }

    /**
     * Reads the raw config string for the given server (useful for debugging).
     */
    public String loadConfigRaw(Server server) {
        if (server == null) return null;
        try {
            return readAssetAsString(server.getConfigFile());
        } catch (IOException e) {
            Log.e(TAG, "Failed to read raw config: " + server.getConfigFile(), e);
            return null;
        }
    }

    /**
     * Checks whether the config file for a given server exists in assets.
     */
    public boolean configExists(Server server) {
        if (server == null || server.getConfigFile() == null) return false;
        try {
            InputStream is = context.getAssets().open(server.getConfigFile());
            is.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    /**
     * Loads a WireGuard Config from an asset file path.
     */
    private Config loadConfigFromAssets(String assetPath) {
        try {
            InputStream inputStream = context.getAssets().open(assetPath);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            Config config = Config.parse(reader);
            reader.close();
            Log.d(TAG, "Config loaded from assets: " + assetPath);
            return config;
        } catch (IOException e) {
            Log.e(TAG, "Failed to open asset: " + assetPath, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse config: " + assetPath, e);
            return null;
        }
    }

    /**
     * Reads an asset file into a String.
     */
    private String readAssetAsString(String assetPath) throws IOException {
        InputStream inputStream = context.getAssets().open(assetPath);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        reader.close();
        return sb.toString();
    }

    // ─── Future: Remote Config Loading ───────────────────────────────────────

    /**
     * TODO: Download config from a remote URL.
     *
     * void downloadConfig(String url, ConfigCallback callback) {
     *     new Thread(() -> {
     *         try {
     *             URL u = new URL(url);
     *             HttpURLConnection conn = (HttpURLConnection) u.openConnection();
     *             InputStream is = conn.getInputStream();
     *             Config config = Config.parse(new BufferedReader(new InputStreamReader(is)));
     *             callback.onSuccess(config);
     *         } catch (Exception e) {
     *             callback.onError(e.getMessage());
     *         }
     *     }).start();
     * }
     */
}
