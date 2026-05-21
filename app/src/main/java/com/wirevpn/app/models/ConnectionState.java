package com.wirevpn.app.models;

/**
 * Represents the current state of the VPN connection.
 */
public enum ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR;

    public boolean isActive() {
        return this == CONNECTING || this == CONNECTED;
    }

    public String getDisplayText() {
        switch (this) {
            case CONNECTED:     return "Connected";
            case CONNECTING:    return "Connecting...";
            case DISCONNECTING: return "Disconnecting...";
            case ERROR:         return "Connection Error";
            default:            return "Disconnected";
        }
    }
}
