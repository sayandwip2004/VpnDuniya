package com.wirevpn.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.wirevpn.app.R;
import com.wirevpn.app.databinding.ItemServerBinding;
import com.wirevpn.app.models.Server;

import java.util.List;

/**
 * RecyclerView adapter using Android Data Binding.
 *
 * Key differences from the old adapter:
 *  - No more findViewById() anywhere — the binding object exposes all views directly.
 *  - server, isSelected, and clickHandler variables are set on the binding and
 *    evaluated by the layout XML itself (see item_server.xml <data> block).
 *  - bind() is a one-liner: set variables + executePendingBindings().
 */
public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {

    // ─── Listener Interface ──────────────────────────────────────────────────

    public interface OnServerClickListener {
        void onServerClick(Server server, int position);
    }

    // ─── Fields ──────────────────────────────────────────────────────────────

    private final List<Server> servers;
    private int selectedPosition;
    private OnServerClickListener clickListener;

    public ServerAdapter(List<Server> servers, int selectedPosition) {
        this.servers          = servers;
        this.selectedPosition = selectedPosition;
    }

    public void setOnServerClickListener(OnServerClickListener listener) {
        this.clickListener = listener;
    }

    // ─── RecyclerView.Adapter ────────────────────────────────────────────────

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate via DataBindingUtil so we get an ItemServerBinding back
        ItemServerBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_server,
                parent,
                false
        );
        return new ServerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        Server server = servers.get(position);
        boolean isSelected = (position == selectedPosition);

        // Pass a wrapped listener that also updates the selection state
        OnServerClickListener wrappedListener = (s, ignored) -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Only redraw the two rows that changed — efficient partial update
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (clickListener != null) {
                clickListener.onServerClick(s, selectedPosition);
            }
        };

        holder.bind(server, isSelected, wrappedListener);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class ServerViewHolder extends RecyclerView.ViewHolder {

        // The binding object replaces all the individual TextView/ImageView fields.
        // Every view in item_server.xml is accessible as binding.<viewId>.
        private final ItemServerBinding binding;

        ServerViewHolder(@NonNull ItemServerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a server row entirely through Data Binding variables.
         * No setText / setVisibility calls needed here — the XML handles it.
         *
         * @param server      The server to display
         * @param isSelected  Whether this row should be highlighted
         * @param listener    Click handler forwarded to the layout's onClick
         */
        void bind(Server server, boolean isSelected, OnServerClickListener listener) {
            binding.setServer(server);
            binding.setIsSelected(isSelected);
            binding.setClickHandler(listener);

            // Force immediate evaluation so views update before the frame renders
            binding.executePendingBindings();
        }
    }
}
