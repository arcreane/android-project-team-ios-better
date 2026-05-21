# Project Pitch: AroundMe

## Description

AroundMe is an Android application that lets users discover local events around their current GPS location, save favorites, and receive notifications before an event starts.

## Key Features

- Map view displaying events near the user's location, filtered by distance
- Event list with search and category filter accessible from the action bar menu
- Favorite system allowing users to bookmark events and review them in a dedicated screen
- Notification scheduled before a saved event starts
- Detail screen opening the event venue in Google Maps via an implicit Intent

## Requirements Mapping

| Requirement | Feature in AroundMe |
|---|---|
| R1 - Multiple Activities and Intents | MainActivity hosts the map, EventDetailActivity shows full event info; an implicit Intent opens Google Maps for directions |
| R2 - Orientation handling | The event list screen adapts from a single-column layout in portrait to a two-column grid in landscape; ViewModel preserves list state across rotation |
| R3 - Fragment | The map view is implemented as a SupportMapFragment embedded in MainActivity; the selected event is communicated back to the Activity via the FragmentResult API |
| R4 - Menu | An options menu in the action bar provides category filters (Music, Sports, Art, All) that update the displayed event list in real time |
| R5 - Advanced view component | The event list is rendered by a RecyclerView with a custom adapter and a custom item layout showing thumbnail, title, date, and distance |
| R6 - Themes and styles | A single theme defined in themes.xml and component styles in styles.xml are applied across all screens; no hard-coded colors appear in any layout file |
| R7 - Phone sensor or hardware | GPS location is retrieved via the FusedLocationProviderClient with a runtime permission request; location is used to center the map and compute event distances |
| R8 - System-level component | A foreground Service manages the notification scheduling: it runs independently of the UI lifecycle, monitors the time remaining before saved events, and fires a notification at the configured threshold |

## External Libraries
Soon...

## Risk Assessment

The main uncertainty is the foreground Service for notification scheduling. Keeping a Service alive under Android's battery optimizations (app standby) and correctly targeting Android 14 foreground service types requires careful reading of the official documentation. A fallback using WorkManager is considered if the Service approach proves unreliable within the project time frame.