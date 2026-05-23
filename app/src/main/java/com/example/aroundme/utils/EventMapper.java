package com.example.aroundme.utils;

import com.example.aroundme.data.remote.dto.TmEvent;
import com.example.aroundme.data.remote.dto.TmVenue;
import com.example.aroundme.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventMapper {

    public static Event fromTmEvent(TmEvent tm) {
        Event event = new Event();
        event.setId(tm.id);
        event.setName(tm.name != null ? tm.name : "Unknown Event");

        if (tm.images != null) {
            for (TmEvent.TmImage img : tm.images) {
                if ("16_9".equals(img.ratio)) {
                    event.setImageUrl(img.url);
                    break;
                }
            }
            if (event.getImageUrl() == null && !tm.images.isEmpty()) {
                event.setImageUrl(tm.images.get(0).url);
            }
        }

        if (tm.dates != null && tm.dates.start != null) {
            event.setDate(tm.dates.start.localDate);
            event.setTime(tm.dates.start.localTime);
        }

        // Venue
        if (tm.embedded != null && tm.embedded.venues != null && !tm.embedded.venues.isEmpty()) {
            TmVenue venue = tm.embedded.venues.get(0);
            event.setVenueName(venue.name);
            if (venue.address != null) event.setVenueAddress(venue.address.line1);
            if (venue.location != null) {
                try {
                    event.setLatitude(Double.parseDouble(venue.location.latitude));
                    event.setLongitude(Double.parseDouble(venue.location.longitude));
                } catch (NumberFormatException ignored) {}
            }
        }

        // Category
        if (tm.classifications != null && !tm.classifications.isEmpty()
                && tm.classifications.get(0).segment != null) {
            event.setCategory(tm.classifications.get(0).segment.name);
        } else {
            event.setCategory("Undefined");
        }

        return event;
    }

    public static List<Event> fromTmEventList(List<TmEvent> tmEvents) {
        List<Event> result = new ArrayList<>();
        if (tmEvents == null) return result;
        for (TmEvent tm : tmEvents) {
            result.add(fromTmEvent(tm));
        }
        return result;
    }
}