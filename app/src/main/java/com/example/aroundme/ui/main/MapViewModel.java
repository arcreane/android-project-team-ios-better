package com.example.aroundme.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aroundme.model.Event;

import java.util.List;


public class MapViewModel extends ViewModel {

    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();

    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Event>> getEvents() { return events; }
    public LiveData<Event> getSelectedEvent() { return selectedEvent; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setEvents(List<Event> list) { events.setValue(list); }
    public void setSelectedEvent(Event event) { selectedEvent.setValue(event); }
    public void setLoading(boolean loading) { isLoading.setValue(loading); }
    public void setErrorMessage(String msg) { errorMessage.setValue(msg); }
}