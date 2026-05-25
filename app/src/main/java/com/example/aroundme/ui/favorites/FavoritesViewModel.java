package com.example.aroundme.ui.favorites;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aroundme.data.EventRepository;
import com.example.aroundme.model.Event;

import java.util.List;


public class FavoritesViewModel extends AndroidViewModel {

    private final EventRepository repository;
    private final LiveData<List<Event>> favorites;

    public FavoritesViewModel(Application application) {
        super(application);
        repository = new EventRepository(application);
        favorites = repository.getAllFavorites();
    }

    public LiveData<List<Event>> getFavorites() { return favorites; }

    public void deleteFavorite(Event event) {
        repository.deleteFavorite(event);
    }
}