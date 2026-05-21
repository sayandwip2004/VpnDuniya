package com.wirevpn.app.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.wireguard.android.backend.GoBackend;
import com.wirevpn.app.R;
import com.wirevpn.app.adapters.ServerAdapter;
import com.wirevpn.app.databinding.ActivityServerListBinding;
import com.wirevpn.app.managers.ServerManager;
import com.wirevpn.app.models.Server;

public class ServerListActivity extends AppCompatActivity {

    private ActivityServerListBinding binding;
    private ServerManager serverManager;
    private ServerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServerListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Server");
        }

        serverManager = ServerManager.getInstance(this);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new ServerAdapter(
                serverManager.getServers(),
                serverManager.getSelectedServerIndex()
        );

        adapter.setOnServerClickListener((server, position) -> {
            serverManager.setSelectedServer(server);
            Toast.makeText(this, server.getFlagEmoji() + " " + server.getName() + " selected", Toast.LENGTH_SHORT).show();
            // Slight delay so the user sees the highlight before going back
            binding.rvServers.postDelayed(() -> {
                finish();
            }, 300);
        });

        binding.rvServers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServers.setAdapter(adapter);

        // Smooth scroll to selected server
        int selectedIdx = serverManager.getSelectedServerIndex();
        binding.rvServers.scrollToPosition(selectedIdx);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
