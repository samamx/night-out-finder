package com.nightout.controller;

import com.nightout.model.Event;
import com.nightout.model.PriceFilter;
import com.nightout.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/search")
    public List<Event> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "any") String budget,
            @RequestParam(defaultValue = "5") int results) {

        PriceFilter priceFilter = parseBudget(budget);
        return eventService.findEvents(keyword, priceFilter, results);
    }

    private PriceFilter parseBudget(String budget) {
        if (budget.equalsIgnoreCase("free")) {
            return PriceFilter.free();
        }
        if (budget.contains("-")) {
            try {
                String[] parts = budget.split("-");
                double min = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());
                return PriceFilter.range(min, max);
            } catch (NumberFormatException e) {
                return PriceFilter.any();
            }
        }
        return PriceFilter.any();
    }

}