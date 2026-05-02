package com.nightout.service;

import com.nightout.client.EventAggregator;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;

import java.util.List;

public class EventService {
    private final EventAggregator aggregator;
    public EventService(EventAggregator aggregator) {
        this.aggregator = aggregator;
    }

    public List<Event> findEvents(String userInput, PriceFilter priceFilter, int perSource) {
        String keyword = extractKeyword(userInput);
        System.out.println("\n Searching for \"" + keyword + "\" in London — " + priceFilter + "...\n");
        return aggregator.search(keyword, priceFilter, perSource);
    }

    private String extractKeyword(String userInput) {
        String[] fillerWords = {"i", "want", "to","see", "go", "a", "an", "the",
                                "some", "feel", "like", "fancy", "looking", "for"};
        String[] words = userInput.toLowerCase().trim().split("\\s+");
        for (String word : words) {
            boolean isFiller = false;
            for (String filler : fillerWords) {
                if (word.equals(filler)) { isFiller = true; break; }
            }
            if (!isFiller && word.length() > 2) return word;
        }
        return userInput.trim();
    }

    public void shutdown() {
            aggregator.shutdown();
    }
}