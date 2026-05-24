package com.example.aroundme.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroundme.R;
import com.example.aroundme.model.Event;
import com.example.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * EventAdapter bridges our List<Event> data and the RecyclerView that displays it.
 *
 * How RecyclerView works:
 *   - RecyclerView asks the adapter how many items there are (getItemCount).
 *   - For each visible row, RecyclerView either creates a new ViewHolder
 *     (onCreateViewHolder) or reuses one that scrolled off screen.
 *   - onBindViewHolder fills the reused/new ViewHolder with data for position i.
 *
 * ViewHolder pattern: we find views by ID once (in the constructor) and store
 * the references. This avoids calling findViewById on every bind, which is slow.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();

    // Callbacks to the Activity — the adapter does not navigate or write to DB itself
    private final OnEventClickListener clickListener;
    private final OnFavoriteClickListener favoriteListener;

    // Track which event IDs are currently favorited so the star icon is correct
    private final java.util.Set<String> favoriteIds = new java.util.HashSet<>();

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Event event, boolean isCurrentlyFavorite);
    }

    public EventAdapter(OnEventClickListener clickListener,
                        OnFavoriteClickListener favoriteListener) {
        this.clickListener = clickListener;
        this.favoriteListener = favoriteListener;
    }

    // --- Data update methods ---

    /**
     * Replace the list and notify the RecyclerView to redraw.
     * Always call this on the main thread.
     */
    public void setEvents(List<Event> newEvents) {
        events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Update the set of favorited IDs so star icons reflect current DB state.
     */
    public void setFavoriteIds(java.util.Set<String> ids) {
        favoriteIds.clear();
        if (ids != null) favoriteIds.addAll(ids);
        notifyDataSetChanged();
    }

    // --- RecyclerView.Adapter methods ---

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and wrap it in a ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, favoriteIds.contains(event.getId()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // --- ViewHolder ---

    class EventViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivThumbnail;
        private final TextView tvName;
        private final TextView tvDate;
        private final TextView tvDistance;
        private final TextView tvCategory;
        private final ImageButton btnFavorite;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views once — store references
            ivThumbnail = itemView.findViewById(R.id.ivEventThumbnail);
            tvName      = itemView.findViewById(R.id.tvEventName);
            tvDate      = itemView.findViewById(R.id.tvEventDate);
            tvDistance  = itemView.findViewById(R.id.tvEventDistance);
            tvCategory  = itemView.findViewById(R.id.tvEventCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(Event event, boolean isFavorite) {
            tvName.setText(event.getName());
            tvDate.setText(event.getDate() != null ? event.getDate() : "Date TBA");
            tvCategory.setText(event.getCategory());

            // Format distance: show "X.X km" or "Nearby" if distance is 0
            if (event.getDistanceKm() > 0) {
                tvDistance.setText(String.format("%.1f km away", event.getDistanceKm()));
            } else {
                tvDistance.setText("Nearby");
            }

            // Load thumbnail with Glide
            // placeholder shown while image loads, error shown if load fails
            Glide.with(itemView.getContext())
                    .load(event.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(ivThumbnail);

            // Star icon state
            btnFavorite.setImageResource(
                    isFavorite ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off);
            btnFavorite.setColorFilter(itemView.getContext().getColor(
                    isFavorite ? R.color.colorFavoriteActive
                            : R.color.colorFavoriteInactive));

            // Click listeners — delegate to the Activity
            itemView.setOnClickListener(v -> clickListener.onEventClick(event));
            btnFavorite.setOnClickListener(v ->
                    favoriteListener.onFavoriteClick(event, isFavorite));
        }
    }
}