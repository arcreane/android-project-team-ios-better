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