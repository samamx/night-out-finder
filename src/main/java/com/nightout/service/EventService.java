package com.nightout.service;

import com.nightout.client.EventAggregator;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;
import com.nightout.nlp.NlpService;

import java.util.List;

public class EventService {

    private final EventAggregator aggregator;
    private final NlpService nlpService;

    public EventService(EventAggregator aggregator, NlpService nlpService) {
        this.aggregator = aggregator;
        this.nlpService = nlpService;
    }

    public List<Event> findEvents(String userInput, PriceFilter priceFilter, int perSource) {
        // Phase 3 — use OpenAI to extract keyword instead of simple extraction
        String keyword = nlpService.extractKeyword(userInput);
        System.out.println("\n🔍 Searching for \"" + keyword + "\" in London — " + priceFilter + "...\n");
        return aggregator.search(keyword, priceFilter, perSource);
    }

    public void shutdown() {
        aggregator.shutdown();
    }
}