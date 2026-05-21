package com.wirevpn.app.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.wirevpn.app.models.Server;

import java.util.ArrayList;
import java.util.List;

/**
 * ServerManager maintains the list of available VPN servers and
 * persists the user's selected server across app restarts.
 *
 * Future: Replace getDefaultServers() with an API call to load
 *         servers dynamically from a remote endpoint.
 */
public class ServerManager {

    private static final String PREFS_NAME     = "wirevpn_prefs";
    private static final String KEY_SERVER_ID  = "selected_server_id";
    private static final String DEFAULT_SERVER = "sg";

    private static ServerManager instance;

    private final SharedPreferences prefs;
    private final List<Server> servers;

    // Private constructor (singleton)
    private ServerManager(Context context) {
        prefs   = context.getApplicationContext()
                         .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        servers = buildDefaultServerList();
    }

    /** Returns the singleton instance. */
    public static synchronized ServerManager getInstance(Context context) {
        if (instance == null) {
            instance = new ServerManager(context);
        }
        return instance;
    }

    // ─── Server List ──────────────────────────────────────────────────────────

    /**
     * Returns the full list of available servers.
     * Extend this list or replace with API-driven data in the future.
     */
    public List<Server> getServers() {
        return servers;
    }

    /**
     * Builds the default (hardcoded) server list.
     * Each server maps to a .conf file in assets/configs/.
     */
    private List<Server> buildDefaultServerList() {
        List<Server> list = new ArrayList<>();

        list.add(new Server(
                "sg",
                "Singapore",
                "SG",
                "configs/sg.conf",
                "🇸🇬"
        ));

        list.add(new Server(
                "us",
                "United States",
                "US",
                "configs/us.conf",
                "🇺🇸"
        ));

        list.add(new Server(
                "mx",
                "Mexico",
                "MX",
                "configs/mex.conf",
                "🇲🇽"
        ));
        list.add(new Server(
                "jp",
                "Japan",
                "JP",
                "configs/jp.conf",
                "🇯🇵"
        ));

        list.add(new Server(
                "nl",
                "Netherlands",
                "NL",
                "configs/nl.conf",
                "🇳🇱"
        ));

        list.add(new Server(
                "ca",
                "Canada",
                "CA",
                "configs/ca.conf",
                "🇨🇦"
        ));
        list.add(new Server(
                "no",
                "Norway",
                "NO",
                "configs/no.conf",
                "🇳🇴"
        ));

        list.add(new Server(
                "pl",
                "Poland",
                "PL",
                "configs/pl.conf",
                "🇵🇱"
        ));

        list.add(new Server(
                "ro",
                "Romania",
                "RO",
                "configs/ro.conf",
                "🇷🇴"
        ));

// 🇨🇭 Switzerland
        list.add(new Server(
                "ch",
                "Switzerland",
                "CH",
                "configs/ch.conf",
                "🇨🇭"
        ));



        return list;
    }

    // ─── Selected Server ──────────────────────────────────────────────────────

    /**
     * Returns the currently selected server (defaults to Singapore).
     */
    public Server getSelectedServer() {
        String savedId = prefs.getString(KEY_SERVER_ID, DEFAULT_SERVER);
        for (Server server : servers) {
            if (server.getId().equals(savedId)) {
                return server;
            }
        }
        // Fallback: return the first server in the list
        return servers.isEmpty() ? null : servers.get(0);
    }

    /**
     * Persists the selected server by its ID.
     */
    public void setSelectedServer(Server server) {
        if (server != null) {
            prefs.edit()
                 .putString(KEY_SERVER_ID, server.getId())
                 .apply();
        }
    }

    /**
     * Returns the index of the selected server in the list (for highlighting).
     */
    public int getSelectedServerIndex() {
        Server selected = getSelectedServer();
        if (selected == null) return 0;
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).getId().equals(selected.getId())) {
                return i;
            }
        }
        return 0;
    }

    // ─── Future: API Integration ──────────────────────────────────────────────

    /**
     * TODO: Replace buildDefaultServerList() with a network call.
     *
     * Example:
     *   void loadServersFromApi(ApiCallback callback) {
     *       RetrofitClient.getApi().getServers().enqueue(...);
     *   }
     */
}
