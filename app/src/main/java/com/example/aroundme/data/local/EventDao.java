package com.example.aroundme.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.aroundme.model.Event;

import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(Event event);

    @Delete
    void deleteFavorite(Event event);

    @Query("SELECT * FROM favorites ORDER BY date ASC")
    LiveData<List<Event>> getAllFavorites();


    @Query("SELECT * FROM favorites ORDER BY date ASC")
    List<Event> getAllFavoritesNow();

    @Query("SELECT * FROM favorites WHERE id = :eventId LIMIT 1")
    Event getFavoriteById(String eventId);
}