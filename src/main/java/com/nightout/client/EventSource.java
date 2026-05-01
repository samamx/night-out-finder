package com.nightout.client;

import com.nightout.model.Event;
import com.nightout.model.PriceFilter;
import java.util.List;

public interface EventSource {

    String getSourceName();
    List<Event> searchEvents(String keyword, PriceFilter priceFilter, int maxResults) throws Exception;
}