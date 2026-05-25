package com.example.aroundme.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.aroundme.BuildConfig;
import com.example.aroundme.data.local.AppDatabase;
import com.example.aroundme.data.local.EventDao;
import com.example.aroundme.data.remote.ApiClient;
import com.example.aroundme.data.remote.TicketmasterApi;
import com.example.aroundme.data.remote.dto.TicketmasterResponse;
import com.example.aroundme.model.Event;
import com.example.aroundme.utils.EventMapper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private static final String TAG = "EventRepository";

    private final TicketmasterApi api;
    private final EventDao eventDao;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public EventRepository(Context context) {
        api = ApiClient.getApi();
        eventDao = AppDatabase.getInstance(context).eventDao();
    }

    public void fetchNearbyEvents(double lat, double lng, int radiusKm,
                                  String category, RepoCallback<List<Event>> callback) {

        android.util.Log.d(TAG, "Fetching events at lat=" + lat + " lng=" + lng);

        String apiKey = BuildConfig.TM_API_KEY;
        String latLong = lat + "," + lng;
        String categoryParam = (category == null || category.isEmpty()) ? null : category;

        api.searchEvents(apiKey, latLong, 100, "km", "FR", categoryParam, 50, "*")
                .enqueue(new Callback<TicketmasterResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<TicketmasterResponse> call,
                                           @NonNull Response<TicketmasterResponse> response) {
                        android.util.Log.d(TAG, "Response code: " + response.code());
                        if (response.isSuccessful() && response.body() != null
                                && response.body().embedded != null) {
                            List<Event> events = EventMapper.fromTmEventList(
                                    response.body().embedded.events);
                            android.util.Log.d(TAG, "Events received: " + events.size());
                            callback.onSuccess(events);
                        } else {
                            android.util.Log.d(TAG, "Empty or failed response: " + response.code());
                            callback.onError("No events found or API error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TicketmasterResponse> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "API call failed", t);
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void insertFavorite(Event event) {
        executor.execute(() -> eventDao.insertFavorite(event));
    }

    public void deleteFavorite(Event event) {
        executor.execute(() -> eventDao.deleteFavorite(event));
    }

    public androidx.lifecycle.LiveData<List<Event>> getAllFavorites() {
        return eventDao.getAllFavorites();
    }

    public void isFavorite(String eventId, RepoCallback<Boolean> callback) {
        executor.execute(() -> {
            Event found = eventDao.getFavoriteById(eventId);
            callback.onSuccess(found != null);
        });
    }


    public interface RepoCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }
}