package com.example.aroundme;

import com.example.aroundme.model.Event;
import com.example.aroundme.service.NotificationService;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NotificationServiceDateTest {

    @Test
    public void pastEvent_returnsNegative() {
        Event event = new Event();
        event.setDate("2000-01-01");
        event.setTime("00:00:00");

        assertTrue(NotificationService.minutesUntilEvent(event) < 0);
    }

    @Test
    public void futureEvent_returnsPositive() {
        Event event = new Event();
        event.setDate("2099-12-31");
        event.setTime("23:59:00");

        assertTrue(NotificationService.minutesUntilEvent(event) > 0);
    }

    @Test
    public void nullDate_returnsMinusOne() {
        Event event = new Event();
        event.setDate(null);

        assertEquals(-1, NotificationService.minutesUntilEvent(event));
    }

    @Test
    public void invalidDateFormat_returnsMinusOne() {
        Event event = new Event();
        event.setDate("not-a-date");

        assertEquals(-1, NotificationService.minutesUntilEvent(event));
    }
}