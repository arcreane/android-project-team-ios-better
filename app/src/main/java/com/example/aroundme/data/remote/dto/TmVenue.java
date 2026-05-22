package com.example.aroundme.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TmVenue {

    @SerializedName("name")
    public String name;

    @SerializedName("address")
    public TmAddress address;

    @SerializedName("location")
    public TmLocation location;

    public static class TmAddress {
        @SerializedName("line1")
        public String line1;
    }

    public static class TmLocation {
        @SerializedName("latitude")
        public String latitude;
        @SerializedName("longitude")
        public String longitude;
    }
}