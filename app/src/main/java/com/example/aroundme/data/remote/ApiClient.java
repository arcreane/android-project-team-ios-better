package com.example.aroundme.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://app.ticketmaster.com/discovery/v2/";
    private static Retrofit retrofitInstance;

    public static Retrofit getClient() {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }

    public static TicketmasterApi getApi() {
        return getClient().create(TicketmasterApi.class);
    }
}