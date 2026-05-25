package com.example.aroundme.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.aroundme.R;
import com.example.aroundme.data.EventRepository;
import com.example.aroundme.model.Event;
import com.example.aroundme.service.NotificationService;
import com.google.android.material.button.MaterialButton;

public class EventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_NAME = "event_name";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_EVENT_TIME = "event_time";
    public static final String EXTRA_VENUE_NAME = "venue_name";
    public static final String EXTRA_VENUE_ADDRESS = "venue_address";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_DISTANCE = "distance_km";

    private EventRepository repository;
    private Event currentEvent;
    private boolean isFavorite = false;
    private MaterialButton btnSaveFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Event Details");
        }

        repository = new EventRepository(this);
        currentEvent = buildEventFromIntent(getIntent());

        bindViews();
        checkIfFavorite();
    }

    private Event buildEventFromIntent(Intent intent) {
        Event e = new Event();
        e.setId(intent.getStringExtra(EXTRA_EVENT_ID));
        e.setName(intent.getStringExtra(EXTRA_EVENT_NAME));
        e.setDate(intent.getStringExtra(EXTRA_EVENT_DATE));
        e.setTime(intent.getStringExtra(EXTRA_EVENT_TIME));
        e.setVenueName(intent.getStringExtra(EXTRA_VENUE_NAME));
        e.setVenueAddress(intent.getStringExtra(EXTRA_VENUE_ADDRESS));
        e.setLatitude(intent.getDoubleExtra(EXTRA_LATITUDE, 0));
        e.setLongitude(intent.getDoubleExtra(EXTRA_LONGITUDE, 0));
        e.setImageUrl(intent.getStringExtra(EXTRA_IMAGE_URL));
        e.setCategory(intent.getStringExtra(EXTRA_CATEGORY));
        e.setDistanceKm(intent.getDoubleExtra(EXTRA_DISTANCE, 0));
        return e;
    }

    private void bindViews() {
        ImageView ivImage = findViewById(R.id.ivDetailImage);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvTime = findViewById(R.id.tvDetailTime);
        TextView tvVenueName = findViewById(R.id.tvDetailVenueName);
        TextView tvVenueAddress = findViewById(R.id.tvDetailVenueAddress);
        MaterialButton btnMaps = findViewById(R.id.btnOpenMaps);
        btnSaveFavorite = findViewById(R.id.btnSaveFavorite);

        tvName.setText(currentEvent.getName());
        tvCategory.setText(currentEvent.getCategory());
        tvDate.setText("Date: " + (currentEvent.getDate() != null
                ? currentEvent.getDate() : "TBA"));
        tvTime.setText("Time: " + (currentEvent.getTime() != null
                ? currentEvent.getTime() : "TBA"));
        tvVenueName.setText(currentEvent.getVenueName());

        if (currentEvent.getDistanceKm() > 0) {
            tvVenueAddress.setText(currentEvent.getVenueAddress()
                    + "\n" + String.format("%.1f km from your location",
                    currentEvent.getDistanceKm()));
        } else {
            tvVenueAddress.setText(currentEvent.getVenueAddress());
        }

        Glide.with(this)
                .load(currentEvent.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivImage);

        btnMaps.setOnClickListener(v -> openInGoogleMaps());
        btnSaveFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void openInGoogleMaps() {
        double lat = currentEvent.getLatitude();
        double lng = currentEvent.getLongitude();
        String label = Uri.encode(currentEvent.getVenueName() != null
                ? currentEvent.getVenueName() : "Venue");

        Uri geoUri = Uri.parse("geo:" + lat + "," + lng
                + "?q=" + lat + "," + lng + "(" + label + ")");

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        mapsIntent.setPackage("com.google.android.apps.maps");

        if (mapsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapsIntent);
        } else {
            mapsIntent.setPackage(null);
            if (mapsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapsIntent);
            } else {
                Toast.makeText(this, "No maps application found.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkIfFavorite() {
        repository.isFavorite(currentEvent.getId(),
                new EventRepository.RepoCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        isFavorite = result;
                        runOnUiThread(() -> updateFavoriteButton());
                    }
                    @Override
                    public void onError(String message) { /* ignore */ }
                });
    }

    private void toggleFavorite() {
        if (isFavorite) {
            repository.deleteFavorite(currentEvent);
            isFavorite = false;
            Toast.makeText(this, "Removed from favorites",
                    Toast.LENGTH_SHORT).show();
        } else {
            repository.insertFavorite(currentEvent);
            isFavorite = true;
            Toast.makeText(this, "Saved to favorites",
                    Toast.LENGTH_SHORT).show();
            NotificationService.startService(this, currentEvent);
        }
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        btnSaveFavorite.setText(isFavorite ? "Remove" : "Save");
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