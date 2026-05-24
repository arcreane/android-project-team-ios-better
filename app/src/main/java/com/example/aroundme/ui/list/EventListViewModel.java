package com.example.aroundme.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aroundme.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the event list for EventListActivity.
 *
 * Why keep allEvents AND filteredEvents separately?
 * When the user picks a category from the menu, we filter in memory
 * without making a new API call. allEvents is the full dataset;
 * filteredEvents is what the RecyclerView sees.
 */
public class EventListViewModel extends ViewModel {

    private final MutableLiveData<List<Event>> filteredEvents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // The unfiltered master list — never shown directly to the UI
    private List<Event> allEvents = new ArrayList<>();

    // The category currently active ("" means all)
    private String currentCategory = "";

    public LiveData<List<Event>> getFilteredEvents() { return filteredEvents; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public String getCurrentCategory() { return currentCategory; }

    public void setLoading(boolean loading) { isLoading.setValue(loading); }
    public void setErrorMessage(String msg) { errorMessage.setValue(msg); }

    /**
     * Store the full list and apply the current filter immediately.
     * Called once after the API returns results.
     */
    public void setAllEvents(List<Event> events) {
        allEvents = events != null ? events : new ArrayList<>();
        applyFilter(currentCategory);
    }

    /**
     * Change the active category and re-filter the existing list.
     * No network call is needed — we already have all the data.
     */
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