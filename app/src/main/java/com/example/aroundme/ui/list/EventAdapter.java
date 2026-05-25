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

import java.util.ArrayList;
import java.util.List;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();

    private final OnEventClickListener clickListener;
    private final OnFavoriteClickListener favoriteListener;

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


    public void setEvents(List<Event> newEvents) {
        events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setFavoriteIds(java.util.Set<String> ids) {
        favoriteIds.clear();
        if (ids != null) favoriteIds.addAll(ids);
        notifyDataSetChanged();
    }

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


    class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvDate;
        private final TextView tvDistance;
        private final TextView tvCategory;
        private final ImageButton btnFavorite;
        private final View viewCategoryBar;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvEventName);
            tvDate         = itemView.findViewById(R.id.tvEventDate);
            tvDistance     = itemView.findViewById(R.id.tvEventDistance);
            tvCategory     = itemView.findViewById(R.id.tvEventCategory);
            btnFavorite    = itemView.findViewById(R.id.btnFavorite);
            viewCategoryBar = itemView.findViewById(R.id.viewCategoryBar);
        }

        void bind(Event event, boolean isFavorite) {
            tvName.setText(event.getName());
            tvDate.setText(event.getDate() != null ? event.getDate() : "Date TBA");
            tvCategory.setText(event.getCategory());

            if (event.getDistanceKm() > 0) {
                tvDistance.setText(String.format("%.1f km away", event.getDistanceKm()));
            } else if (event.getVenueName() != null) {
                tvDistance.setText(event.getVenueName());
            } else {
                tvDistance.setText("Venue TBA");
            }

            // Color the left bar by category
            int barColor;
            if (event.getCategory() == null) {
                barColor = android.graphics.Color.parseColor("#1565C0");
            } else {
                switch (event.getCategory().toLowerCase()) {
                    case "music":
                        barColor = android.graphics.Color.parseColor("#1565C0");
                        break;
                    case "sports":
                        barColor = android.graphics.Color.parseColor("#2E7D32");
                        break;
                    case "arts & theatre":
                    case "arts":
                        barColor = android.graphics.Color.parseColor("#6A1B9A");
                        break;
                    default:
                        barColor = android.graphics.Color.parseColor("#E65100");
                        break;
                }
            }
            viewCategoryBar.setBackgroundColor(barColor);

            btnFavorite.setImageResource(
                    isFavorite ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off);
            btnFavorite.setColorFilter(itemView.getContext().getColor(
                    isFavorite ? R.color.colorFavoriteActive
                            : R.color.colorFavoriteInactive));

            itemView.setOnClickListener(v -> clickListener.onEventClick(event));
            btnFavorite.setOnClickListener(v ->
                    favoriteListener.onFavoriteClick(event, isFavorite));
        }
    }
}