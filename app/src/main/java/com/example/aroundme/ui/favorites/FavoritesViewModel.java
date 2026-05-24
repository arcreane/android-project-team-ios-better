package com.example.aroundme.ui.favorites;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aroundme.data.EventRepository;
import com.example.aroundme.model.Event;

import java.util.List;

/**
 * FavoritesViewModel extends AndroidViewModel (not plain ViewModel)
 * because it needs an Application context to create the Repository.
 *
 * The favorites list comes directly from Room as LiveData.
 * Room updates the LiveData automatically whenever the table changes —
 * we never need to manually refresh the list.
 */
public class FavoritesViewModel extends AndroidViewModel {

    private final EventRepository repository;
    private final LiveData<List<Event>> favorites;

    public FavoritesViewModel(Application application) {
        super(application);
        repository = new EventRepository(application);
        // Room's LiveData — auto-updates when the favorites table changes
        favorites = repository.getAllFavorites();
    }

    public LiveData<List<Event>> getFavorites() { return favorites; }

    public void deleteFavorite(Event event) {
        repository.deleteFavorite(event);
    }
}