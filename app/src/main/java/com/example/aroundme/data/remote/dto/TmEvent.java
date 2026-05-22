package com.example.aroundme.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TmEvent {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("images")
    public List<TmImage> images;

    @SerializedName("dates")
    public TmDates dates;

    @SerializedName("_embedded")
    public TmEmbedded embedded;

    @SerializedName("classifications")
    public List<TmClassification> classifications;

    public static class TmImage {
        @SerializedName("url")
        public String url;
        @SerializedName("ratio")
        public String ratio;
    }

    public static class TmDates {
        @SerializedName("start")
        public TmStart start;

        public static class TmStart {
            @SerializedName("localDate")
            public String localDate;
            @SerializedName("localTime")
            public String localTime;
        }
    }

    public static class TmEmbedded {
        @SerializedName("venues")
        public List<TmVenue> venues;
    }

    public static class TmClassification {
        @SerializedName("segment")
        public TmSegment segment;

        public static class TmSegment {
            @SerializedName("name")
            public String name;
        }
    }
}