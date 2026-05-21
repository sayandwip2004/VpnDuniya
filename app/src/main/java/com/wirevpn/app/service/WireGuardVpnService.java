package com.wirevpn.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.wirevpn.app.R;
import com.wirevpn.app.activities.MainActivity;
import com.wirevpn.app.models.ConnectionState;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;

/**
 * WireGuardVpnService wraps the WireGuard GoBackend and manages
 * tunnel lifecycle: connect, disconnect, status tracking.
 *
 * This is a singleton-style manager (not a bound Android Service per se)
 * that the Activities interact with via static methods and a listener interface.
 *
 * The actual VPN tunneling is done via WireGuard's GoBackend which internally
 * creates an Android VpnService tunnel.
 */
public class WireGuardVpnService {

    private static final String TAG             = "WireGuardVpnService";
    private static final String TUNNEL_NAME     = "wg0";
    private static final String CHANNEL_ID      = "wirevpn_channel";
    private static final int    NOTIFICATION_ID = 1001;

    private static WireGuardVpnService instance;

    private final Context context;
    private GoBackend backend;
    private Tunnel tunnel;
    private ConnectionState currentState = ConnectionState.DISCONNECTED;
    private ConnectionStateListener listener;

    // Prevent duplicate connection attempts
    private boolean isConnecting = false;

    private WireGuardVpnService(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized WireGuardVpnService getInstance(Context context) {
        if (instance == null) {
            instance = new WireGuardVpnService(context);
        }
        return instance;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Connects the VPN tunnel with the provided WireGuard config.
     * Runs on a background thread; posts state changes to the listener.
     *
     * @param config The parsed WireGuard configuration.
     */
    public void connect(Config config) {
        if (isConnecting || currentState == ConnectionState.CONNECTED) {
            Log.w(TAG, "connect() ignored – already connecting or connected.");
            return;
        }
        if (config == null) {
            notifyStateChange(ConnectionState.ERROR);
            return;
        }

        isConnecting = true;
        notifyStateChange(ConnectionState.CONNECTING);

        new Thread(() -> {
            try {
                // Initialise GoBackend (creates Android VpnService tunnel)
                if (backend == null) {
                    backend = new GoBackend(context);
                }

                // Create tunnel object
                final Config finalConfig = config;
                tunnel = new Tunnel() {
                    @Override
                    public String getName() {
                        return TUNNEL_NAME;
                    }

                    @Override
                    public void onStateChange(State newState) {
                        Log.d(TAG, "Tunnel state changed: " + newState);
                    }
                };

                // Bring tunnel up
                backend.setState(tunnel, Tunnel.State.UP, finalConfig);
                isConnecting = false;
                notifyStateChange(ConnectionState.CONNECTED);
                showConnectedNotification();
                Log.d(TAG, "WireGuard tunnel connected.");

            } catch (Exception e) {
                Log.e(TAG, "Failed to connect WireGuard tunnel", e);
                isConnecting = false;
                notifyStateChange(ConnectionState.ERROR);
            }
        }, "wireguard-connect").start();
    }

    /**
     * Disconnects the VPN tunnel.
     */
    public void disconnect() {
        if (currentState == ConnectionState.DISCONNECTED) {
            return;
        }

        notifyStateChange(ConnectionState.DISCONNECTING);

        new Thread(() -> {
            try {
                if (backend != null && tunnel != null) {
                    backend.setState(tunnel, Tunnel.State.DOWN, null);
                    Log.d(TAG, "WireGuard tunnel disconnected.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting tunnel", e);
            } finally {
                isConnecting = false;
                tunnel = null;
                notifyStateChange(ConnectionState.DISCONNECTED);
                cancelNotification();
            }
        }, "wireguard-disconnect").start();
    }

    /** Returns the current connection state. */
    public ConnectionState getState() {
        return currentState;
    }

    /** Returns whether the VPN is currently connected. */
    public boolean isConnected() {
        return currentState == ConnectionState.CONNECTED;
    }

    /**
     * Registers a listener for connection state changes.
     * Call from Activity's onResume(); unregister in onPause().
     */
    public void setStateListener(ConnectionStateListener listener) {
        this.listener = listener;
    }

    public void removeStateListener() {
        this.listener = null;
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private void notifyStateChange(ConnectionState newState) {
        currentState = newState;
        if (listener != null) {
            // Post to main thread
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> {
                        if (listener != null) {
                            listener.onStateChanged(newState);
                        }
                    });
        }
    }

    // ─── Notification ─────────────────────────────────────────────────────────

    private void showConnectedNotification() {
        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vpn_key)
                .setContentTitle("WireVPN Active")
                .setContentText("Your connection is protected")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

    private void cancelNotification() {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "VPN Status",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows when WireVPN is active");
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    // ─── Listener Interface ───────────────────────────────────────────────────

    public interface ConnectionStateListener {
        void onStateChanged(ConnectionState state);
    }
}
