package com.example.aroundme.data.remote;

import com.example.aroundme.data.remote.dto.TicketmasterResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TicketmasterApi {

    @GET("events.json")
    Call<TicketmasterResponse> searchEvents(
            @Query("apikey") String apiKey,
            @Query("latlong") String latLong,
            @Query("radius") int radius,
            @Query("unit") String unit,
            @Query("countryCode") String countryCode,
            @Query("classificationName") String classificationName,
            @Query("size") int size,
            @Query("locale") String locale
    );
}