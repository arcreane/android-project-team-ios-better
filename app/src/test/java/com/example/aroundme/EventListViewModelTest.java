package com.example.aroundme;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.aroundme.model.Event;
import com.example.aroundme.ui.list.EventListViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EventListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EventListViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new EventListViewModel();

        Event music = new Event();
        music.setId("1");
        music.setName("Jazz Night");
        music.setCategory("Music");

        Event sports = new Event();
        sports.setId("2");
        sports.setName("Football Cup");
        sports.setCategory("Sports");

        Event arts = new Event();
        arts.setId("3");
        arts.setName("Art Exhibition");
        arts.setCategory("Arts & Theatre");

        viewModel.setAllEvents(Arrays.asList(music, sports, arts));
    }

    @Test
    public void filterAll_returnsAllEvents() {
        viewModel.applyFilter("");
        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void filterMusic_returnsOnlyMusic() {
        viewModel.applyFilter("music");
        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jazz Night", result.get(0).getName());
    }

    @Test
    public void filterSports_returnsOnlySports() {
        viewModel.applyFilter("sports");
        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Football Cup", result.get(0).getName());
    }

    @Test
    public void filterUnknown_returnsEmpty() {
        viewModel.applyFilter("concerts");
        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}