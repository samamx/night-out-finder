package com.nightout.service;

import com.nightout.entity.SavedEvent;
import com.nightout.entity.SearchHistory;
import com.nightout.model.Event;
import com.nightout.repository.SavedEventRepository;
import com.nightout.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseService {
    private final SearchHistoryRepository searchHistoryRepository;
    private final SavedEventRepository savedEventRepository;

    public DatabaseService(SearchHistoryRepository searchHistoryRepository,
                        SavedEventRepository savedEventRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.savedEventRepository = savedEventRepository;
    }

    public void saveSearch(String userInput, String extractedKeyword,
                       String budget, int resultsCount) {
        SearchHistory search = new SearchHistory(userInput, extractedKeyword,
                                                budget, resultsCount);
        searchHistoryRepository.save(search);
        System.out.println("💾 Search saved to database");
    }

    public List<SearchHistory> getSearchHistory() {
        return searchHistoryRepository.findAllByOrderBySearchedAtDesc();
    }

    public String saveEvent(Event event) {
        if (savedEventRepository.existsByUrl(event.getUrl())) {
            return "Event already saved";
        }

        SavedEvent savedEvent = new SavedEvent(
            event.getName(),
            event.getDate(),
            event.getTime(),
            event.getVenueName(),
            event.getVenueAddress(),
            event.getUrl(),
            event.getGenre(),
            event.getPriceRange(),
            event.getSource()
        );

        savedEventRepository.save(savedEvent);
        return "Event saved successfully";
    }

    public List<SavedEvent> getSavedEvents() {
        return savedEventRepository.findAllByOrderBySavedAtDesc();
    }

}