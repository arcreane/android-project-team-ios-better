package com.example.aroundme.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aroundme.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventListViewModel extends ViewModel {

    private final MutableLiveData<List<Event>> filteredEvents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private List<Event> allEvents = new ArrayList<>();

    private String currentCategory = "";

    public LiveData<List<Event>> getFilteredEvents() { return filteredEvents; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public String getCurrentCategory() { return currentCategory; }

    public void setLoading(boolean loading) { isLoading.setValue(loading); }
    public void setErrorMessage(String msg) { errorMessage.setValue(msg); }

    public void setAllEvents(List<Event> events) {
        allEvents = events != null ? events : new ArrayList<>();
        applyFilter(currentCategory);
    }

    public void applyFilter(String category) {
        currentCategory = category;

        if (category == null || category.isEmpty()) {
            filteredEvents.setValue(new ArrayList<>(allEvents));
            return;
        }

        List<Event> result = new ArrayList<>();
        for (Event e : allEvents) {
            if (e.getCategory() != null &&
                    e.getCategory().toLowerCase().contains(category.toLowerCase())) {
                result.add(e);
            }
        }
        filteredEvents.setValue(result);
    }
}