package com.nightout.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nightout.model.Event;
import com.nightout.model.PriceFilter;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SkiddleClient implements EventSource {
    private static final String BASE_URL = "https://www.skiddle.com/api/v1/events/search/";
    private static final String SOURCE_NAME = "Skiddle";
    private static final String LONDON_LAT  = "51.5074";
    private static final String LONDON_LON  = "-0.1278";
    private static final String RADIUS      = "10";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SkiddleClient(String apiKey) {

    this.apiKey = apiKey;
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
public List<Event> searchEvents(String keyword, PriceFilter priceFilter, int maxResults) throws Exception {
    String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

    StringBuilder url = new StringBuilder(String.format(
        "%s?api_key=%s&keyword=%s&latitude=%s&longitude=%s&radius=%s&limit=%d&order=date",
        BASE_URL, apiKey, encodedKeyword, LONDON_LAT, LONDON_LON, RADIUS, maxResults
    ));

    if (priceFilter.isFree()) url.append("&free=1");

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url.toString()))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
        System.err.println("Skiddle error: HTTP " + response.statusCode());
        return new ArrayList<>();
    }

    List<Event> events = parseEvents(response.body());

    if (priceFilter.isRange()) events = filterByRange(events, priceFilter);

    return events;
}

private List<Event> parseEvents(String json) throws Exception {
    List<Event> events = new ArrayList<>();
    JsonNode root = objectMapper.readTree(json);
    JsonNode results = root.path("results");
    if (!results.isArray()) return events;

    for (JsonNode node : results) {
        String name      = node.path("eventname").asText("Unknown Event");
        String url       = node.path("link").asText("#");
        String date      = node.path("date").asText("TBC");
        String time      = node.path("openingtimes").path("doorsopen").asText("TBC");
        String venueName = node.path("venue").path("name").asText("Unknown Venue");
        String venueAddr = node.path("venue").path("address").asText("Unknown Address");
        String genre     = node.path("genres").path(0).path("name").asText("N/A");
        String price     = parsePriceRange(node);

        events.add(new Event(name, date, time, venueName, venueAddr,
                             url, genre, price, SOURCE_NAME));
    }
    return events;
}

private String parsePriceRange(JsonNode node) {
    String entry = node.path("entryprice").asText("").trim();
    if (entry.isEmpty() || entry.equals("null")) return "Price TBC";
    try {
        double price = Double.parseDouble(entry);
        return price == 0 ? "Free" : String.format("£%.0f", price);
    } catch (NumberFormatException e) {
        return "Price TBC";
    }
}

private List<Event> filterByRange(List<Event> events, PriceFilter filter) {
    List<Event> filtered = new ArrayList<>();
    for (Event e : events) {
        String pr = e.getPriceRange();
        if (pr.equals("Price TBC") || pr.equals("Free")) {
            filtered.add(e);
            continue;
        }
        try {
            double price = Double.parseDouble(pr.replace("£", "").trim());
            if (price >= filter.getMinPrice() && price <= filter.getMaxPrice()) {
                filtered.add(e);
            }
        } catch (NumberFormatException ex) {
            filtered.add(e);
        }
    }
    return filtered;
}

}