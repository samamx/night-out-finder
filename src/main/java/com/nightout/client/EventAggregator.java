package com.nightout.client;

import com.nightout.model.Event;
import com.nightout.model.PriceFilter;

import java.util.*;
import java.util.concurrent.*;

public class EventAggregator {

    private final List<EventSource> sources;
    private final ExecutorService executor;

    public EventAggregator(List<EventSource> sources) {
    this.sources = sources;
    this.executor = Executors.newFixedThreadPool(sources.size());
    }

    public List<Event> search(String keyword, PriceFilter priceFilter, int perSource) {
        List<Future<List<Event>>> futures = sources.stream()
            .map(source -> executor.submit(() -> {
                try {
                    return source.searchEvents(keyword, priceFilter, perSource);
                } catch (Exception e) {
                    System.err.println(" " + source.getSourceName() + " failed: " + e.getMessage());
                    return Collections.<Event>emptyList();
                }
            }))
            .toList();

        List<Event> allEvents = new ArrayList<>();
        for (Future<List<Event>> future : futures) {
            try {
                allEvents.addAll(future.get(10, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                System.err.println("A source timed out and was skipped.");
            } catch (Exception e) {
                System.err.println("Error collecting results: " + e.getMessage());
            }
        }

        return deduplicate(allEvents);
    }

    private List<Event> deduplicate(List<Event> events) {
    Set<String> seen = new HashSet<>();
    List<Event> unique = new ArrayList<>();

    for (Event event : events) {
        String name = event.getName().toLowerCase().replaceAll("[^a-z0-9 ]", "");
        String key  = (name.length() > 40 ? name.substring(0, 40) : name) + "|" + event.getDate();

        if (seen.add(key)) unique.add(event);
    }

    unique.sort(Comparator.comparing(e -> e.getDate().equals("TBC") ? "9999" : e.getDate()));
    return unique;
    }

    public void shutdown() {
        executor.shutdown();
    }


}