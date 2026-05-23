package com.example.aroundme;

import com.example.aroundme.data.remote.dto.TmEvent;
import com.example.aroundme.data.remote.dto.TmVenue;
import com.example.aroundme.model.Event;
import com.example.aroundme.utils.EventMapper;

import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class EventMapperTest {

    @Test
    public void testBasicMapping() {
        TmEvent tm = new TmEvent();
        tm.id = "abc123";
        tm.name = "Rock Festival";

        tm.dates = new TmEvent.TmDates();
        tm.dates.start = new TmEvent.TmDates.TmStart();
        tm.dates.start.localDate = "2025-09-14";
        tm.dates.start.localTime = "20:00:00";

        tm.embedded = new TmEvent.TmEmbedded();
        TmVenue venue = new TmVenue();
        venue.name = "Stade de France";
        venue.address = new TmVenue.TmAddress();
        venue.address.line1 = "93200 Saint-Denis";
        venue.location = new TmVenue.TmLocation();
        venue.location.latitude = "48.9244";
        venue.location.longitude = "2.3601";
        tm.embedded.venues = new ArrayList<>();
        tm.embedded.venues.add(venue);

        Event event = EventMapper.fromTmEvent(tm);

        assertEquals("abc123", event.getId());
        assertEquals("Rock Festival", event.getName());
        assertEquals("2025-09-14", event.getDate());
        assertEquals("Stade de France", event.getVenueName());
        assertEquals(48.9244, event.getLatitude(), 0.0001);
    }

    @Test
    public void testNullImagesDoNotCrash() {
        TmEvent tm = new TmEvent();
        tm.id = "x";
        tm.name = "No Image Event";
        tm.images = null;

        Event event = EventMapper.fromTmEvent(tm);
        assertNull(event.getImageUrl());
    }

    @Test
    public void testDefaultCategoryWhenMissing() {
        TmEvent tm = new TmEvent();
        tm.id = "y";
        tm.name = "Mystery Event";
        tm.classifications = null;

        Event event = EventMapper.fromTmEvent(tm);
        assertEquals("Undefined", event.getCategory());
    }
}