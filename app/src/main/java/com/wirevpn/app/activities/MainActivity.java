package com.wirevpn.app.activities;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.wirevpn.app.R;
import com.wirevpn.app.databinding.ActivityMainBinding;
import com.wirevpn.app.managers.ConfigManager;
import com.wirevpn.app.managers.ServerManager;
import com.wirevpn.app.models.ConnectionState;
import com.wirevpn.app.models.Server;
import com.wirevpn.app.service.WireGuardVpnService;
import com.wirevpn.app.utils.PreferenceUtils;
import com.wireguard.config.Config;

/**
 * MainActivity: The primary screen showing VPN status,
 * selected server, and connect/disconnect controls.
 */
public class MainActivity extends AppCompatActivity
        implements WireGuardVpnService.ConnectionStateListener {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private WireGuardVpnService vpnService;
    private ServerManager       serverManager;
    private ConfigManager       configManager;

    // Launcher for VPN permission dialog
    private final ActivityResultLauncher<Intent> vpnPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // Permission granted – proceed with connection
                            startVpnConnection();
                        } else {
                            Toast.makeText(this,
                                    "VPN permission denied. Cannot connect.",
                                    Toast.LENGTH_SHORT).show();
                            updateUiForState(ConnectionState.DISCONNECTED);
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize managers
        vpnService    = WireGuardVpnService.getInstance(this);
        serverManager = ServerManager.getInstance(this);
        configManager = new ConfigManager(this);

        setupClickListeners();
        updateSelectedServerDisplay();
        updateUiForState(vpnService.getState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        vpnService.setStateListener(this);
        // Refresh UI in case state changed while activity was paused
        updateUiForState(vpnService.getState());
        updateSelectedServerDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vpnService.removeStateListener();
    }

    // ─── UI Setup ─────────────────────────────────────────────────────────────

    private void setupClickListeners() {
        // Connect / Disconnect button
        binding.btnConnect.setOnClickListener(v -> {
            onConnectButtonClicked();
        });

        // Server selection row
        binding.layoutServerSelect.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServerListActivity.class);
            startActivity(intent);
        });
    }

    private void updateSelectedServerDisplay() {
        Server server = serverManager.getSelectedServer();
        if (server != null) {
            binding.tvSelectedFlag.setText(server.getFlagEmoji());
            binding.tvSelectedServer.setText(server.getName());
            binding.tvSelectedCode.setText(server.getCountryCode());
        }
    }

    // ─── Connection Logic ─────────────────────────────────────────────────────

    private void onConnectButtonClicked() {
        ConnectionState state = vpnService.getState();

        if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
            // Already connected – disconnect
            vpnService.disconnect();
            PreferenceUtils.setWasConnected(this, false);
        }else if (state == ConnectionState.DISCONNECTED || state == ConnectionState.ERROR) {
            // Need to connect – check VPN permission first
            Intent permIntent = VpnService.prepare(this);
            if (permIntent != null) {
                // Must ask user for permission
                vpnPermissionLauncher.launch(permIntent);
            } else {
                // Already have permission
                startVpnConnection();
            }
        }
    }

    private void startVpnConnection() {
        Server selectedServer = serverManager.getSelectedServer();
        if (selectedServer == null) {
            Toast.makeText(this, "No server selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!configManager.configExists(selectedServer)) {
            Toast.makeText(this,
                    "Config file not found for " + selectedServer.getName(),
                    Toast.LENGTH_LONG).show();
            updateUiForState(ConnectionState.ERROR);
            return;
        }

        Config config = configManager.loadConfig(selectedServer);
        if (config == null) {
            Toast.makeText(this,
                    "Invalid config for " + selectedServer.getName(),
                    Toast.LENGTH_LONG).show();
            updateUiForState(ConnectionState.ERROR);
            return;
        }

        PreferenceUtils.setWasConnected(this, true);
        vpnService.connect(config);
        Log.d(TAG, "Connecting to: " + selectedServer.getName());
    }

    // ─── ConnectionStateListener ──────────────────────────────────────────────

    @Override
    public void onStateChanged(ConnectionState state) {
        updateUiForState(state);
    }

    // ─── UI State Updates ─────────────────────────────────────────────────────

    private void updateUiForState(ConnectionState state) {
        // Status text
        binding.tvStatus.setText(state.getDisplayText());

        // Status badge color
        int badgeColor;
        switch (state) {
            case CONNECTED:
                badgeColor = getColor(R.color.status_connected);
                break;
            case CONNECTING:
            case DISCONNECTING:
                badgeColor = getColor(R.color.status_connecting);
                break;
            case ERROR:
                badgeColor = getColor(R.color.status_error);
                break;
            default:
                badgeColor = getColor(R.color.status_disconnected);
                break;
        }
        binding.viewStatusDot.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(badgeColor));

        // Loading spinner visibility
        binding.progressConnecting.setVisibility(
                (state == ConnectionState.CONNECTING || state == ConnectionState.DISCONNECTING)
                        ? View.VISIBLE : View.GONE);

        // Connect button state
        boolean isBusy = state == ConnectionState.CONNECTING
                || state == ConnectionState.DISCONNECTING;

        binding.btnConnect.setEnabled(!isBusy);

        if (state == ConnectionState.CONNECTED) {
            binding.btnConnect.setText(R.string.disconnect);
            binding.btnConnect.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.btn_disconnect)));
        } else if (isBusy) {
            binding.btnConnect.setText(
                    state == ConnectionState.CONNECTING
                            ? R.string.connecting : R.string.disconnecting);
        } else {
            binding.btnConnect.setText(R.string.connect);
            binding.btnConnect.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.btn_connect)));
        }

        // VPN shield icon (connected vs disconnected look)
        binding.ivVpnShield.setImageResource(
                state == ConnectionState.CONNECTED
                        ? R.drawable.ic_shield_on
                        : R.drawable.ic_shield_off);

        // Big connection ring animation
        if (state == ConnectionState.CONNECTED) {
            binding.viewConnectionRing.setVisibility(View.VISIBLE);
        } else {
            binding.viewConnectionRing.setVisibility(View.INVISIBLE);
        }
    }
}
