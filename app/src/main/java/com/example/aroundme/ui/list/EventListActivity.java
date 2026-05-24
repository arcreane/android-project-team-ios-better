package com.example.aroundme.ui.list;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroundme.R;
import com.example.aroundme.data.EventRepository;
import com.example.aroundme.model.Event;
import com.example.aroundme.ui.detail.EventDetailActivity;
import com.example.aroundme.ui.favorites.FavoritesActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventListActivity extends AppCompatActivity {

    private EventListViewModel viewModel;
    private EventRepository repository;
    private EventAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private double lat = 0;
    private double lng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Events Near You");
        }

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        viewModel = new ViewModelProvider(this).get(EventListViewModel.class);
        repository = new EventRepository(this);

        bindViews();
        setupRecyclerView();
        observeViewModel();

        if (savedInstanceState == null) {
            loadEvents();
        }
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.recyclerViewEvents);
        progressBar  = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(
                this::onEventClicked,
                this::onFavoriteClicked
        );

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getFilteredEvents().observe(this, events -> {
            adapter.setEvents(events);
            tvEmptyState.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            refreshFavoriteIcons(events);
        });
    }

    private void loadEvents() {
        if (lat == 0 && lng == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Open this screen from the map to see events near you.");
            return;
        }

        viewModel.setLoading(true);
        repository.fetchNearbyEvents(lat, lng, 10, "", new EventRepository.RepoCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                for (Event e : result) {
                    float[] distResult = new float[1];
                    android.location.Location.distanceBetween(
                            lat, lng,
                            e.getLatitude(), e.getLongitude(),
                            distResult);
                    e.setDistanceKm(distResult[0] / 1000.0);
                }
                viewModel.setLoading(false);
                viewModel.setAllEvents(result);
            }

            @Override
            public void onError(String message) {
                viewModel.setLoading(false);
                viewModel.setErrorMessage(message);
            }
        });
    }

    private void refreshFavoriteIcons(List<Event> events) {
        new Thread(() -> {
            Set<String> favIds = new HashSet<>();
            for (Event e : events) {
                com.example.aroundme.data.local.AppDatabase db =
                        com.example.aroundme.data.local.AppDatabase.getInstance(this);
                if (db.eventDao().getFavoriteById(e.getId()) != null) {
                    favIds.add(e.getId());
                }
            }
            runOnUiThread(() -> adapter.setFavoriteIds(favIds));
        }).start();
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
        if (isCurrentlyFavorite) {
            repository.deleteFavorite(event);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            repository.insertFavorite(event);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }

        recyclerView.postDelayed(() -> {
            List<Event> current = viewModel.getFilteredEvents().getValue();
            if (current != null) refreshFavoriteIcons(current);
        }, 300);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        } else if (id == R.id.filter_all) {
            viewModel.applyFilter("");
        } else if (id == R.id.filter_music) {
            viewModel.applyFilter("music");
        } else if (id == R.id.filter_sports) {
            viewModel.applyFilter("sports");
        } else if (id == R.id.filter_arts) {
            viewModel.applyFilter("arts");
        }
        return true;
    }
}