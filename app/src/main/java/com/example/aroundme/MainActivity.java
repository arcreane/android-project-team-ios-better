package com.example.aroundme.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.aroundme.R;
import com.example.aroundme.data.EventRepository;
import com.example.aroundme.model.Event;
import com.example.aroundme.ui.detail.EventDetailActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapViewModel viewModel;
    private EventRepository repository;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private ProgressBar progressBar;
    private FloatingActionButton fabMyLocation;

    private View panelSelectedEvent;
    private TextView tvPanelEventName;
    private TextView tvPanelEventDate;
    private TextView tvPanelVenueName;

    private final Map<Marker, Event> markerEventMap = new HashMap<>();

    private String currentCategory = "";

    private Location lastLocation;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        Boolean fine = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        if (Boolean.TRUE.equals(fine)) {
                            // Permission granted — get the location and load events
                            getLastLocation();
                        } else {
                            Toast.makeText(this,
                                    "Location permission is required to show events near you.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        repository = new EventRepository(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        progressBar = findViewById(R.id.progressBar);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        panelSelectedEvent = findViewById(R.id.panelSelectedEvent);
        tvPanelEventName = findViewById(R.id.tvPanelEventName);
        tvPanelEventDate = findViewById(R.id.tvPanelEventDate);
        tvPanelVenueName = findViewById(R.id.tvPanelVenueName);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fabMyLocation.setOnClickListener(v -> {
            if (lastLocation != null) {
                centerMapOn(lastLocation.getLatitude(), lastLocation.getLongitude());
            } else {
                requestLocationPermission();
            }
        });

        observeViewModel();
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

        viewModel.getEvents().observe(this, events -> {
            if (events != null && googleMap != null) {
                placeMarkersOnMap(events);
            }
        });

        viewModel.getSelectedEvent().observe(this, event -> {
            if (event != null) {
                showEventPanel(event);
            }
        });
    }


    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }


    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        lastLocation = location;
                        centerMapOn(location.getLatitude(), location.getLongitude());
                        loadEvents(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Could not get location. Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void centerMapOn(double lat, double lng) {
        if (googleMap == null) return;
        LatLng position = new LatLng(lat, lng);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }


        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            Event event = markerEventMap.get(marker);
            if (event != null) {
                viewModel.setSelectedEvent(event);
            }
            return false;
        });

        List<Event> existing = viewModel.getEvents().getValue();
        if (existing != null) {
            placeMarkersOnMap(existing);
        }


        requestLocationPermission();
    }

    private void placeMarkersOnMap(List<Event> events) {
        googleMap.clear();
        markerEventMap.clear();

        for (Event event : events) {
            if (event.getLatitude() == 0 && event.getLongitude() == 0) continue;

            LatLng position = new LatLng(event.getLatitude(), event.getLongitude());

            MarkerOptions options = new MarkerOptions()
                    .position(position)
                    .title(event.getName())
                    .snippet(event.getDate())
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            categoryToHue(event.getCategory())));

            Marker marker = googleMap.addMarker(options);
            if (marker != null) {
                markerEventMap.put(marker, event);
            }
        }
    }

    private float categoryToHue(String category) {
        if (category == null) return BitmapDescriptorFactory.HUE_RED;
        switch (category.toLowerCase()) {
            case "music":  return BitmapDescriptorFactory.HUE_AZURE;
            case "sports": return BitmapDescriptorFactory.HUE_GREEN;
            case "arts":
            case "arts & theatre": return BitmapDescriptorFactory.HUE_VIOLET;
            default:       return BitmapDescriptorFactory.HUE_RED;
        }
    }

    // API call

    private void loadEvents(double lat, double lng) {
        viewModel.setLoading(true);
        repository.fetchNearbyEvents(lat, lng, 10, currentCategory, new EventRepository.RepoCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                viewModel.setLoading(false);
                viewModel.setEvents(result);
            }

            @Override
            public void onError(String message) {
                viewModel.setLoading(false);
                viewModel.setErrorMessage(message);
            }
        });
    }

    // Landscape side panel


    private void showEventPanel(Event event) {
        if (panelSelectedEvent != null) {

            panelSelectedEvent.setVisibility(View.VISIBLE);
            tvPanelEventName.setText(event.getName());
            tvPanelEventDate.setText(event.getDate());
            tvPanelVenueName.setText(event.getVenueName());

            View btnDetail = findViewById(R.id.btnPanelViewDetail);
            if (btnDetail != null) {
                btnDetail.setOnClickListener(v -> openDetailActivity(event));
            }
        } else {
            openDetailActivity(event);
        }
    }

    private void openDetailActivity(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        // Pass the event ID; the detail activity fetches full data from the repository
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getId());
        // We also pass the object fields directly to avoid a second DB/API call
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_filter_all) {
            currentCategory = "";
        } else if (id == R.id.menu_filter_music) {
            currentCategory = "music";
        } else if (id == R.id.menu_filter_sports) {
            currentCategory = "sports";
        } else if (id == R.id.menu_filter_arts) {
            currentCategory = "arts";
        } else if (id == R.id.menu_open_list) {
            Intent intent = new Intent(this, com.example.aroundme.ui.list.EventListActivity.class);
            if (lastLocation != null) {
                intent.putExtra("lat", lastLocation.getLatitude());
                intent.putExtra("lng", lastLocation.getLongitude());
            }
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

        // Reload events with the new category filter
        if (lastLocation != null) {
            loadEvents(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        return true;
    }
}