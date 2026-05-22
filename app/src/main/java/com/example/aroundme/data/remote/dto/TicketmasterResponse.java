package com.example.aroundme.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TicketmasterResponse {

    @SerializedName("_embedded")
    public Embedded embedded;

    public static class Embedded {
        @SerializedName("events")
        public List<TmEvent> events;
    }
}