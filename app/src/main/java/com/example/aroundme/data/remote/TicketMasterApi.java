package com.example.aroundme.data.remote;

import com.example.aroundme.data.remote.dto.TicketmasterResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TicketMasterApi {
    @GET("event.json")
    Call<TicketmasterResponse> searchEvents(
        @Query("apikey") String apiKey,
        @Query("latlong") String latLong,
        @Query("radius") int radius,
        @Query("unit") String unit,
        @Query("size") int size,
        @Query("classificationName") String classificationName
    );


}
