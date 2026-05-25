package com.example.aroundme.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroundme.R;
import com.example.aroundme.model.Event;
import com.example.aroundme.ui.detail.EventDetailActivity;
import com.example.aroundme.ui.list.EventAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private FavoritesViewModel viewModel;
    private EventAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favorites");
        }

        tvEmpty = findViewById(R.id.tvFavoritesEmpty);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewFavorites);

        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        adapter = new EventAdapter(
                this::onEventClicked,
                this::onFavoriteClicked
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel.getFavorites().observe(this, favorites -> {
            adapter.setEvents(favorites);

            Set<String> favIds = new HashSet<>();
            for (Event e : favorites) favIds.add(e.getId());
            adapter.setFavoriteIds(favIds);

            tvEmpty.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void onEventClicked(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_NAME, event.getName());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, event.getDate());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TIME, event.getTime());
        intent.putExtra(EventDetailActivity.EXTRA_VENUE_NAME, event.getVenueName());
        intent.putExtra(EventDetailActivity.EXTRA_VENUE_ADDRESS, event.getVenueAddress());
        intent.putExtra(EventDetailActivity.EXTRA_LATITUDE, event.getLatitude());
        intent.putExtra(EventDetailActivity.EXTRA_LONGITUDE, event.getLongitude());
        intent.putExtra(EventDetailActivity.EXTRA_IMAGE_URL, event.getImageUrl());
        intent.putExtra(EventDetailActivity.EXTRA_CATEGORY, event.getCategory());
        startActivity(intent);
    }

    private void onFavoriteClicked(Event event, boolean isCurrentlyFavorite) {
        viewModel.deleteFavorite(event);
        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}