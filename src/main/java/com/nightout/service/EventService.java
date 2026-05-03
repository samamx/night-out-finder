package com.nightout.service;

import com.nightout.client.EventAggregator;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;
import com.nightout.nlp.NlpService;

import java.util.List;

public class EventService {

    private final EventAggregator aggregator;
    private final NlpService nlpService;
    private final DatabaseService databaseService;

    public EventService(EventAggregator aggregator, NlpService nlpService,
                        DatabaseService databaseService) {
        this.aggregator = aggregator;
        this.nlpService = nlpService;
        this.databaseService = databaseService;
    }

    public List<Event> findEvents(String userInput, PriceFilter priceFilter, int perSource) {
        String keyword = nlpService.extractKeyword(userInput);
        System.out.println("\nSearching for \"" + keyword + "\" in London — " + priceFilter + "...\n");

        List<Event> events = aggregator.search(keyword, priceFilter, perSource);

        // Save search to database automatically
        databaseService.saveSearch(userInput, keyword, priceFilter.toString(), events.size());

        return events;
    }

    public void shutdown() {
        aggregator.shutdown();
    }
}