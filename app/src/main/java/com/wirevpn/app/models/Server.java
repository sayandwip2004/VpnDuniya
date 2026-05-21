package com.wirevpn.app.models;

public class Server {

    private String id;
    private String name;
    private String countryCode;
    private String configFile;    // Asset filename or remote URL
    private String flagEmoji;     // Unicode flag emoji
    private boolean isPremium;
    private int pingMs;           // Future: server speed in ms (-1 = unknown)

    /**
     * Full constructor
     */
    public Server(String id, String name, String countryCode,
                  String configFile, String flagEmoji, boolean isPremium) {
        this.id = id;
        this.name = name;
        this.countryCode = countryCode;
        this.configFile = configFile;
        this.flagEmoji = flagEmoji;
        this.isPremium = isPremium;
        this.pingMs = -1;
    }

    /**
     * Convenience constructor for free servers
     */
    public Server(String id, String name, String countryCode,
                  String configFile, String flagEmoji) {
        this(id, name, countryCode, configFile, flagEmoji, false);
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public String getId() { return id; }

    public String getName() { return name; }

    public String getCountryCode() { return countryCode; }

    public String getConfigFile() { return configFile; }

    public String getFlagEmoji() { return flagEmoji; }

    public boolean isPremium() { return isPremium; }

    public int getPingMs() { return pingMs; }

    // ─── Setters ───────────────────────────────────────────────────────────────

    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public void setConfigFile(String configFile) { this.configFile = configFile; }

    public void setFlagEmoji(String flagEmoji) { this.flagEmoji = flagEmoji; }

    public void setPremium(boolean premium) { isPremium = premium; }

    public void setPingMs(int pingMs) { this.pingMs = pingMs; }

    // ─── Utility ──────────────────────────────────────────────────────────────

    /**
     * Returns the ping as a human-readable string.
     */
    public String getPingDisplay() {
        if (pingMs < 0) return "";
        return pingMs + " ms";
    }

    /**
     * Whether the config is a remote URL (starts with http/https).
     */
    public boolean isRemoteConfig() {
        return configFile != null &&
               (configFile.startsWith("http://") || configFile.startsWith("https://"));
    }

    @Override
    public String toString() {
        return "Server{id='" + id + "', name='" + name + "', countryCode='" + countryCode + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server server = (Server) o;
        return id != null && id.equals(server.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
