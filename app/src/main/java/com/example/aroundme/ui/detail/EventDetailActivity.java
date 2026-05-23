package com.example.aroundme.ui.detail;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aroundme.R;

public class EventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID      = "event_id";
    public static final String EXTRA_EVENT_NAME    = "event_name";
    public static final String EXTRA_EVENT_DATE    = "event_date";
    public static final String EXTRA_EVENT_TIME    = "event_time";
    public static final String EXTRA_VENUE_NAME    = "venue_name";
    public static final String EXTRA_VENUE_ADDRESS = "venue_address";
    public static final String EXTRA_LATITUDE      = "latitude";
    public static final String EXTRA_LONGITUDE     = "longitude";
    public static final String EXTRA_IMAGE_URL     = "image_url";
    public static final String EXTRA_CATEGORY      = "category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }
}